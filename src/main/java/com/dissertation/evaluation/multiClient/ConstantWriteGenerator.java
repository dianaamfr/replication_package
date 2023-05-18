package com.dissertation.evaluation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.WriteRequestLog;
import com.dissertation.evaluation.logs.WriteResponseLog;
import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class ConstantWriteGenerator {
    private final ScheduledThreadPoolExecutor scheduler;
    private final long delay;
    private final List<CountDownLatch> countdowns;
    private final CountDownLatch startSignal;
    private final List<ArrayDeque<Log>> logs;
    private final ByteString payload;
    private final List<String> keys;
    private final AtomicIntegerArray keyCounters;

    private static final String USAGE = "Usage: ConstantWriteGenerator <regionPartitions:Int> " +
            "<readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ " +
            "<delay:Int> <writesPerClient:Int> <keysPerPartition:String> <clients:Int>";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int writesPerClient, int keysPerPartition, int clients) {
        this.scheduler = scheduler;
        this.delay = delay;
        this.countdowns = new ArrayList<>();
        this.startSignal = new CountDownLatch(1);
        this.logs = new ArrayList<>();
        this.payload = Utils.byteStringFromString(String.valueOf(Utils.PAYLOAD_START_LONG));
        this.keys = Utils.generateKeys(writeAddresses, keysPerPartition);
        this.keyCounters = new AtomicIntegerArray(clients);
        this.init(clients, writesPerClient, readAddress, writeAddresses);
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

            if (writesPerClient % regionPartitions != 0) {
                System.err.println("Writes per client must be a multiple of region partitions");
                return;
            }

            if (keysPerPartition * regionPartitions > 26) {
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

    private void init(int clients, int writesPerClient, Address readAddress, List<Address> writeAddresses) {
        for (int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            ArrayDeque<Log> logs = new ArrayDeque<>(Utils.MAX_LOGS);
            this.keyCounters.set(i, i % this.keys.size());
            CountDownLatch countdown = new CountDownLatch(writesPerClient);
            this.countdowns.add(countdown);
            this.logs.add(logs);
            this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(client, countdown, logs, i), 0, this.delay,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            for (CountDownLatch countDown : this.countdowns) {
                countDown.await();
            }
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            Utils.logsToFile(this.logs, String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WriteGeneratorRequest implements Runnable {
        private final Client client;
        private final CountDownLatch countDown;
        private final ArrayDeque<Log> logs;
        private final int index;

        public WriteGeneratorRequest(Client client, CountDownLatch countDown, ArrayDeque<Log> logs, int index) {
            this.client = client;
            this.countDown = countDown;
            this.logs = logs;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                startSignal.await();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for start signal");
                return;
            }

            if (this.countDown.getCount() > 0) {
                String key = keys.get(keyCounters.get(this.index));
                int partitionId = Utils.getKeyPartitionId(key);

                long t1 = System.currentTimeMillis();
                WriteResponse writeResponse;
                try {
                    writeResponse = client.requestWrite(key, payload);
                } catch (Exception e) {
                    incrementKeyCounter(this.index);
                    Utils.printException(e);
                    return;
                }
                long t2 = System.currentTimeMillis();

                this.logs.add(new WriteRequestLog(partitionId, writeResponse.getWriteTimestamp(), t1));
                this.logs.add(new WriteResponseLog(partitionId, writeResponse.getWriteTimestamp(), t2));

                this.countDown.countDown();
                incrementKeyCounter(this.index);
            }
        }
    }

    private void incrementKeyCounter(int index) {
        this.keyCounters.set(index, (this.keyCounters.get(index) + 1) % this.keys.size());
    }
}
