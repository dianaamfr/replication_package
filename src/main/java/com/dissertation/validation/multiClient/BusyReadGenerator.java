package com.dissertation.validation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.ROTRequestLog;
import com.dissertation.validation.logs.ROTResponseLog;

public class BusyReadGenerator {
    private ExecutorService executor;
    private final long endMarker;
    private final List<ArrayDeque<Log>> logs;
    private final int readSetsSize;
    private final CountDownLatch startSignal;
    private final CountDownLatch countDown;

    private static final String USAGE = "Usage: BusyReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <expectedWrites:Int> <keysPerRead:Int> <keysPerPartition:String> <clients:Int>";

    public BusyReadGenerator(ExecutorService executor, Address readAddress, List<Address> writeAddresses, long endMarker, int keysPerRead, int keysPerPartition, int clients) {
        this.executor = executor;
        this.endMarker = endMarker;
        this.logs = new ArrayList<>();
        this.startSignal = new CountDownLatch(1);
        this.countDown = new CountDownLatch(1);
        List<Set<String>> readSets = Utils.getReadSets(Utils.generateKeys(writeAddresses, keysPerPartition), keysPerRead);
        this.readSetsSize = readSets.size();
        this.init(clients, readSets, readAddress, writeAddresses);
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

            long expectedWrites = Long.parseLong(args[addressesEndIndex]);
            long endMarker = Utils.PAYLOAD_START_LONG + expectedWrites - 1;
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerPartition = Integer.parseInt(args[addressesEndIndex + 2]);
            int clients = Integer.parseInt(args[addressesEndIndex + 3]);

            ExecutorService executor = Executors.newFixedThreadPool(clients);

            BusyReadGenerator reader = new BusyReadGenerator(executor, readAddress, writeAddresses, endMarker, keysPerRead, keysPerPartition, clients);
            reader.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(USAGE);
        }
    }

    public void init(int clients, List<Set<String>> keys, Address readAddress, List<Address> writeAddresses) {
        System.out.println(keys);
        for(int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            ArrayDeque<Log> logs = new ArrayDeque<>(Utils.MAX_LOGS);
            int startIndex = i % keys.size();
            this.logs.add(logs);
            this.executor.submit(new ReadGeneratorRequest(client, keys, logs, startIndex));
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            Utils.logsToFile(this.logs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        private final Client client;
        private final List<Set<String>> readSets;
        private final ArrayDeque<Log> logs;
        private final int startIndex;
        private long lastPayload;
        private int keyCounter;

        public ReadGeneratorRequest(Client client, List<Set<String>> keys, ArrayDeque<Log> logs, int startIndex) {
            this.client = client;
            this.readSets = keys;
            this.logs = logs;
            this.startIndex = startIndex;
            this.lastPayload = Utils.PAYLOAD_START_LONG - 1;
            this.keyCounter = 0;
        }
        
        @Override    
        public void run() {
            ROTResponse rotResponse;
            long t1, t2;
            String valueStr;
            long valueLong;
            boolean newPayload = false;
            boolean exit = false;

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for start signal");
                return;
            }

            while (countDown.getCount() > 0) {
                Set<String> requestKeys = readSets.get((this.startIndex + this.keyCounter) % readSetsSize);
                t1 = System.currentTimeMillis();
                try {
                    rotResponse = this.client.requestROT(requestKeys);
                    t2 = System.currentTimeMillis();
                } catch (Exception e) {
                    this.keyCounter = incrementKeyCounter(this.keyCounter);
                    Utils.printException(e);
                    continue;
                }

                for (KeyVersion keyVersion: rotResponse.getVersionsMap().values()) {
                    valueStr = Utils.stringFromByteString(keyVersion.getValue());
                    if (valueStr.isBlank()) {
                        continue;
                    }

                    try {
                        valueLong = Long.parseLong(valueStr);
                        if (valueLong > this.lastPayload) {
                            this.lastPayload = valueLong;
                            newPayload = true;
                        }
                        if (valueLong == endMarker) {
                            exit = true;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if (newPayload) {
                    newPayload = false;
                    this.logs.add(new ROTRequestLog(rotResponse.getId(), t1));
                    this.logs.add(new ROTResponseLog(rotResponse.getId(), rotResponse.getStableTime(), t2));
                }

                if (exit) {
                    this.client.shutdown();
                    countDown.countDown();
                    break;
                }
            }
        }
    }

    private int incrementKeyCounter(int keyCounter) {
        return (keyCounter + 1) % this.readSetsSize;
    }
}
