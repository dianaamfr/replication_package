package com.dissertation.evaluation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.ROTRequestLog;
import com.dissertation.evaluation.logs.ROTResponseLog;
import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

public class BusyReadGenerator {
    private final ExecutorService executor;
    private final int readTime;
    private final List<ArrayDeque<Log>> logs;
    private final CountDownLatch startSignal;
    private final List<Set<String>> readSets;

    private static final String USAGE = "Usage: BusyReadGenerator <regionPartitions:Int> " +
            "<readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ " +
            "<readTime:Int> <keysPerRead:Int> <keysPerPartition:String> <clients:Int>";

    public BusyReadGenerator(ExecutorService executor, Address readAddress, List<Address> writeAddresses, int readTime,
            int keysPerRead, int keysPerPartition, int clients) {
        this.executor = executor;
        this.readTime = readTime;
        this.logs = new ArrayList<>();
        this.startSignal = new CountDownLatch(1);
        this.readSets = Utils.getReadSets(Utils.generateKeys(writeAddresses, keysPerPartition), keysPerRead);
        this.init(clients, readAddress, writeAddresses);
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

            int readTime = Integer.parseInt(args[addressesEndIndex]);
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerPartition = Integer.parseInt(args[addressesEndIndex + 2]);
            int clients = Integer.parseInt(args[addressesEndIndex + 3]);

            if ((keysPerPartition * regionPartitions) % keysPerRead != 0) {
                System.err.println("The number of keys must be divisible by the keys per read.");
                return;
            }

            ExecutorService executor = Executors.newFixedThreadPool(clients);

            BusyReadGenerator reader = new BusyReadGenerator(executor, readAddress, writeAddresses, readTime,
                    keysPerRead, keysPerPartition, clients);
            reader.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(USAGE);
        }
    }

    private void init(int clients, Address readAddress, List<Address> writeAddresses) {
        for (int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            ArrayDeque<Log> logs = new ArrayDeque<>(Utils.MAX_LOGS);
            int startIndex = i % this.readSets.size();
            this.logs.add(logs);
            this.executor.submit(new ReadGeneratorRequest(client, logs, startIndex));
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            Utils.logsToFile(this.logs, String.format("%s-%s", Utils.READ_CLIENT_ID, Utils.getCurrentRegion().toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        private final Client client;
        private final ArrayDeque<Log> logs;
        private long lastPayload;
        private int keyCounter;

        public ReadGeneratorRequest(Client client, ArrayDeque<Log> logs, int startIndex) {
            this.client = client;
            this.logs = logs;
            this.lastPayload = Utils.PAYLOAD_START_LONG - 1;
            this.keyCounter = startIndex;
        }

        @Override
        public void run() {
            ROTResponse rotResponse;
            long t1, t2;
            String valueStr;
            long valueLong;
            boolean newPayload = false;

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for start signal");
                return;
            }

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < readTime) {
                Set<String> requestKeys = readSets.get(this.keyCounter);
                t1 = System.currentTimeMillis();
                try {
                    rotResponse = this.client.requestROT(requestKeys);
                    t2 = System.currentTimeMillis();
                } catch (Exception e) {
                    this.keyCounter = incrementKeyCounter(this.keyCounter);
                    Utils.printException(e);
                    continue;
                }

                for (KeyVersion keyVersion : rotResponse.getVersionsMap().values()) {
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
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if (newPayload) {
                    newPayload = false;
                    this.logs.add(new ROTRequestLog(rotResponse.getId(), t1));
                    this.logs.add(new ROTResponseLog(rotResponse.getId(), rotResponse.getStableTime(), t2));
                }

                this.keyCounter = incrementKeyCounter(this.keyCounter);
            }
            this.client.shutdown();
        }
    }

    private int incrementKeyCounter(int keyCounter) {
        return (keyCounter + 1) % this.readSets.size();
    }
}
