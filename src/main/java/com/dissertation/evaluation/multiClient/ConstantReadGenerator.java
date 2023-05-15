package com.dissertation.evaluation.multiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.evaluation.logs.GoodputLog;
import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

public class ConstantReadGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private final long delay;
    private final int readSetsSize;
    private CountDownLatch startSignal;
    private final CountDownLatch countDown;

    private static final String USAGE = "Usage: ConstantReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <totalReads:Int> <keysPerRead:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
    List<Address> writeAddresses, long delay, int totalReads, int keysPerRead, int keysPerPartition, int clients) {
        this.scheduler = scheduler;
        this.delay = delay;
        this.countDown = new CountDownLatch(1);
        this.startSignal = new CountDownLatch(1);


        List<Set<String>> readSets = Utils.getReadSets(Utils.generateKeys(writeAddresses, keysPerPartition), keysPerRead);
        this.readSetsSize = readSets.size();
        this.init(clients, readSets, readAddress, writeAddresses);
    }
    
    private void init(int clients, List<Set<String>> keys, Address readAddress, List<Address> writeAddresses) {
        for(int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            int startIndex = i % keys.size();
            this.scheduler.scheduleWithFixedDelay(new ReadGeneratorRequest(client, keys, startIndex), 0, this.delay, TimeUnit.MILLISECONDS);
        }
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
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

            if (args.length < addressesEndIndex + 5) {
                System.err.println(USAGE);
                return;
            }

            long delay = Long.parseLong(args[addressesEndIndex]);
            int totalReads = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 2]);
            int keysPerPartition = Integer.parseInt(args[addressesEndIndex + 3]);
            int clients = Integer.parseInt(args[addressesEndIndex + 4]);

            // if (keys.size() % keysPerRead != 0) {
            //     System.err.println("The number of keys must be divisible by the keys per read.");
            //     return;
            // }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, readAddress, writeAddresses, delay,
                    totalReads, keysPerRead, keysPerPartition, clients);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void run() {
        try {
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        private final Client client;
        private final List<Set<String>> readSets;
        private final int startIndex;
        private int keyCounter;
        private long lastPayload;
        private long startTime;
        private long endTime;

        public ReadGeneratorRequest(Client client, List<Set<String>> readSets, int startIndex) {
            this.client = client;
            this.readSets = readSets;
            this.startIndex = startIndex;
            this.keyCounter = 0;
            this.lastPayload = 0;
            this.startTime = 0;
            this.endTime = 0;
        }


        @Override
        public void run() {
            boolean newPayload = false;

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (countDown.getCount() > 0) {
                Set<String> requestKeys = readSets.get((this.startIndex + keyCounter) % readSets.size());

                ROTResponse rotResponse;
                try {
                    rotResponse = client.requestROT(requestKeys);
                    endTime = System.currentTimeMillis();
                } catch (Exception e) {
                    keyCounter = incrementKeyCounter(keyCounter);
                    Utils.printException(e);
                    return;
                }

                for (KeyVersion version : rotResponse.getVersionsMap().values()) {
                    String valueStr = Utils.stringFromByteString(version.getValue());
                    if (valueStr.isBlank()) {
                        continue;
                    }

                    long valueLong = Long.parseLong(valueStr);
                    if (valueLong > lastPayload) {
                        lastPayload = valueLong;
                        newPayload = true;
                    }
                }

                if (newPayload) {
                    countDown.countDown();
                    newPayload = false;

                    if(countDown.getCount() == 0) {
                        System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG : 0,
                            endTime - startTime).toJson().toString());
                    }
                }

                keyCounter = incrementKeyCounter(keyCounter);
            }
        }
    }

    private int incrementKeyCounter(int keyCounter) {
        return (keyCounter + 1) % this.readSetsSize;
    }

}
