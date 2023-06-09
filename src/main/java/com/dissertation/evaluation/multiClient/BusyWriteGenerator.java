package com.dissertation.evaluation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.ThroughputLog;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class BusyWriteGenerator {
    private final ExecutorService executor;
    private final int writeTime;
    private final List<CountDownLatch> countdowns;
    private final CountDownLatch startSignal;
    private final ByteString payload;
    private final List<String> keys;
    private final AtomicLong writeCounter;

    private static final String USAGE = "Usage: BusyWriteGenerator <regionPartitions:Int> " +
            "<readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ " +
            "<keysPerPartition:Int> <clients:Int> <writeTime>?";

    public BusyWriteGenerator(ExecutorService executor, Address readAddress, List<Address> writeAddresses,int keysPerPartition, int clients, int writeTime) {
        this.executor = executor;
        this.writeTime = writeTime;
        this.countdowns = new ArrayList<>();
        this.startSignal = new CountDownLatch(1);
        this.payload = Utils.byteStringFromString(String.valueOf(Utils.PAYLOAD_START_LONG));
        this.writeCounter = new AtomicLong(0);
        this.keys = Utils.generateKeys(writeAddresses, keysPerPartition);
        this.init(clients, keysPerPartition, readAddress, writeAddresses);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                executor.shutdownNow();
            }
        });
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 8) {
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

            if (args.length < addressesEndIndex + 2) {
                System.err.println(USAGE);
                return;
            }

            int keysPerPartition = Integer.parseInt(args[addressesEndIndex]);
            int clients = Integer.parseInt(args[addressesEndIndex + 1]);
            int writeTime = args.length > addressesEndIndex + 2 ? Integer.parseInt(args[addressesEndIndex + 2]) : Utils.WRITE_TIME;

            ExecutorService executor = Executors.newFixedThreadPool(clients);

            BusyWriteGenerator writer = new BusyWriteGenerator(executor, readAddress, writeAddresses, keysPerPartition,
                    clients, writeTime);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
            return;
        }
    }

    private void init(int clients, int keysPerPartition, Address readAddress,
            List<Address> writeAddresses) {
        for (int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            CountDownLatch countdown = new CountDownLatch(1);
            this.countdowns.add(countdown);
            int startIndex = i % this.keys.size();
            this.executor.submit(new WriteGeneratorRequest(client, countdown, startIndex));
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            for (CountDownLatch countDown : this.countdowns) {
                countDown.await();
            }
            this.executor.shutdown();
            this.executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

            ArrayDeque<Log> logs = new ArrayDeque<>(1);
            logs.add(new ThroughputLog(writeCounter.get()));
            Utils.logToFile(logs, String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WriteGeneratorRequest implements Runnable {
        private final Client client;
        private final CountDownLatch countdown;
        private int keyCounter;

        public WriteGeneratorRequest(Client client, CountDownLatch countdown, int startIndex) {
            this.client = client;
            this.countdown = countdown;
            this.keyCounter = startIndex;
        }

        @Override
        public void run() {
            String key;
            long clientWriteCount = 0;

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                Utils.printException(e);
                return;
            }

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < writeTime) {
                key = keys.get(this.keyCounter);
                try {
                    this.client.requestWrite(key, payload);
                } catch (Exception e) {
                    this.keyCounter = incrementKeyCounter(this.keyCounter);
                    Utils.printException(e);
                    continue;
                }
                clientWriteCount++;
                this.keyCounter = incrementKeyCounter(this.keyCounter);
            }
            this.countdown.countDown();
            writeCounter.accumulateAndGet(clientWriteCount, Long::sum);
        }
    }

    private int incrementKeyCounter(int keyCounter) {
        return (keyCounter + 1) % this.keys.size();
    }
}
