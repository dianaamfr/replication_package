package com.dissertation.evaluation.singleClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private final Client client;
    private final long delay;
    private final int totalWrites;
    private final List<String> keys;
    private final CountDownLatch countDown;
    private final ArrayDeque<Log> logs;
    private long payload;
    private int keyCounter;

    private static final String USAGE = "Usage: ConstantWriteGenerator <regionPartitions:Int> " +
            "<readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ " +
            "<delay:Int> <totalWrites:Int> <key:String>+";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int totalWrites, List<String> keys) {
        this.scheduler = scheduler;
        this.client = new Client(readAddress, writeAddresses);
        this.delay = delay;
        this.totalWrites = totalWrites;
        this.keys = keys;
        this.countDown = new CountDownLatch(totalWrites);
        this.logs = new ArrayDeque<>(this.totalWrites * 2);
        this.payload = Utils.PAYLOAD_START_LONG;
        this.keyCounter = 0;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Utils.logToFile(logs,
                        String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));
            }
        });
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        if (args.length < 9) {
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

            if (args.length < addressesEndIndex + 3) {
                System.err.println(USAGE);
                return;
            }

            long delay = Long.parseLong(args[addressesEndIndex]);
            int totalWrites = Integer.parseInt(args[addressesEndIndex + 1]);
            for (int i = addressesEndIndex + 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantWriteGenerator writer = new ConstantWriteGenerator(scheduler, readAddress, writeAddresses, delay,
                    totalWrites, keys);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    public void run() {
        this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS);

        try {
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            this.client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WriteGeneratorRequest implements Runnable {

        @Override
        public void run() {
            String key = keys.get(keyCounter);
            int partitionId = Utils.getKeyPartitionId(key);

            if (keyCounter < totalWrites) {
                ByteString value = Utils.byteStringFromString(String.valueOf(payload));

                long t1 = System.currentTimeMillis();
                WriteResponse writeResponse;
                try {
                    writeResponse = client.requestWrite(key, value);
                } catch (Exception e) {
                    keyCounter = incrementKeyCounter();
                    Utils.printException(e);
                    return;
                }
                long t2 = System.currentTimeMillis();

                logs.add(new WriteRequestLog(partitionId, writeResponse.getWriteTimestamp(), t1));
                logs.add(new WriteResponseLog(partitionId, writeResponse.getWriteTimestamp(), t2));

                keyCounter = incrementKeyCounter();
                payload++;
                countDown.countDown();
            }
        }
    }

    private int incrementKeyCounter() {
        return (this.keyCounter + 1) % this.keys.size();
    }
}
