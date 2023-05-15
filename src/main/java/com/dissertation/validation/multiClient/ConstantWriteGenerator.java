package com.dissertation.validation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.WriteRequestLog;
import com.dissertation.validation.logs.WriteResponseLog;
import com.google.protobuf.ByteString;

public class ConstantWriteGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private final long delay;
    private List<CountDownLatch> countdowns;
    private CountDownLatch startSignal;
    private List<ArrayDeque<Log>> logs;
    private AtomicLong payload;
    
    private static final String USAGE = "Usage: ConstantWriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <writesPerClient:Int> <keysPerPartition:String> <clients:Int>";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int writesPerClient, int keysPerPartition, int clients) {
        this.delay = delay;
        this.scheduler = scheduler;
        this.startSignal = new CountDownLatch(1);
        this.countdowns = new ArrayList<>();
        this.logs = new ArrayList<>();
        this.payload = new AtomicLong(Utils.PAYLOAD_START_LONG);

        List<String> keys = Utils.generateKeys(writeAddresses, keysPerPartition);
        this.init(clients, writesPerClient, keys, readAddress, writeAddresses);
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 10) {
            System.err.println(USAGE);
            return;
        }

        try {
            regionPartitions = Integer.parseInt(args[0]);
            readAddress = new Address(Integer.parseInt(args[1]), args[2]);
            int addressesEndIndex = regionPartitions * 3 + 3;
            for (int i = 3; i < addressesEndIndex; i += 3) {
                writeAddresses.add(new Address(Integer.parseInt(args[i]), args[i + 1], Integer.parseInt(args[i + 2])));
            }

            if (args.length < addressesEndIndex + 4) {
                System.err.println(USAGE);
                return;
            }

            long delay = Long.parseLong(args[addressesEndIndex]);
            int writesPerClient = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerPartition = Integer.parseInt(args[addressesEndIndex + 2]);
            int clients = Integer.parseInt(args[addressesEndIndex + 3]);

            if(writesPerClient % regionPartitions != 0) {
                System.err.println("Writes per client must be a multiple of region partitions");
                return;
            }

            if(keysPerPartition * regionPartitions > 26) {
                System.err.println("The total number of keys must be less than 26");
                return;
            }

            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(clients);

            ConstantWriteGenerator writer = new ConstantWriteGenerator(scheduler, readAddress, writeAddresses, delay,
                    writesPerClient, keysPerPartition, clients);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void init(int clients, int writesPerClient, List<String> keys, Address readAddress, List<Address> writeAddresses) {
        for(int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            ArrayDeque<Log> logs = new ArrayDeque<>(Utils.MAX_LOGS);
            int startIndex = i % keys.size();
            CountDownLatch countdown = new CountDownLatch(writesPerClient);
            this.countdowns.add(countdown);
            this.logs.add(logs);
            this.scheduler.scheduleAtFixedRate(new WriteGeneratorRequest(client, countdown, keys, logs, startIndex), 0, this.delay, TimeUnit.MILLISECONDS);
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            for(CountDownLatch countDown : this.countdowns) {
                countDown.await();
            }
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            Utils.logsToFile(this.logs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private class WriteGeneratorRequest implements Runnable {
        private Client client;
        private CountDownLatch countDown;
        private List<String> keys;
        private ArrayDeque<Log> logs;
        private int startIndex;

        public WriteGeneratorRequest(Client client, CountDownLatch countDown, List<String> keys, ArrayDeque<Log> logs, int startIndex) {
            this.client = client;
            this.countDown = countDown;
            this.keys = keys;
            this.logs = logs;
            this.startIndex = startIndex;
        }

        @Override
        public void run() {
            try {
                startSignal.await();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for start signal");
                return;
            }
            
            int count = (int) this.countDown.getCount();
            if (count > 0) {
                String key = this.keys.get((count + this.startIndex) % this.keys.size());
                long valuePayload = payload.getAndIncrement();
                ByteString value = Utils.byteStringFromString(String.valueOf(valuePayload));
                int partitionId = Utils.getKeyPartitionId(key);

                long t1 = System.currentTimeMillis();
                WriteResponse writeResponse;
                try {
                    writeResponse = client.requestWrite(key, value);
                } catch (Exception e) {
                    Utils.printException(e);
                    return;
                }
                long t2 = System.currentTimeMillis();

                this.logs.add(new WriteRequestLog(partitionId, writeResponse.getWriteTimestamp(), t1));
                this.logs.add(new WriteResponseLog(partitionId, writeResponse.getWriteTimestamp(), t2));

                this.countDown.countDown();
            }
        }
    }
}
