package com.dissertation.validation.singleClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.GoodputLog;

public class ConstantReadGenerator {
    private final ScheduledThreadPoolExecutor scheduler;
    private final Client client;
    private final long delay;
    private final List<Set<String>> readSets;
    private final CountDownLatch countDown;
    private int keyCounter;
    private long lastPayload;
    private long startTime;
    private long endTime;

    private static final String USAGE = "Usage: ConstantReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <totalReads:Int> <keysPerRead:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int totalReads, int keysPerRead, List<String> keys) {
        this.scheduler = scheduler;
        this.client = new Client(readAddress, writeAddresses);
        this.delay = delay;
        this.readSets = Utils.getReadSets(keys, keysPerRead);
        this.countDown = new CountDownLatch(totalReads);
        this.keyCounter = 0;
        this.lastPayload = 0;
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();
        List<String> keys = new ArrayList<>();

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
            int totalReads = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 2]);
            for (int i = addressesEndIndex + 3; i < args.length; i++) {
                keys.add(args[i]);
            }

            if (keys.size() % keysPerRead != 0) {
                System.err.println("The number of keys must be divisible by the keys per read.");
                return;
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, readAddress, writeAddresses, delay,
                    totalReads, keysPerRead, keys);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void run() {
        this.startTime = System.currentTimeMillis();
        this.scheduler.scheduleWithFixedDelay(new ReadGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS);

        try {
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            this.client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        @Override
        public void run() {
            boolean newPayload = false;

            if (countDown.getCount() > 0) {
                Set<String> requestKeys = readSets.get(keyCounter % readSets.size());

                ROTResponse rotResponse;
                try {
                    rotResponse = client.requestROT(requestKeys);
                    endTime = System.currentTimeMillis();
                } catch (Exception e) {
                    keyCounter = incrementKeyCounter();
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

                    if (countDown.getCount() == 0) {
                        System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG : 0,
                                endTime - startTime).toJson().toString());
                    }
                }

                keyCounter = incrementKeyCounter();
            }
        }
    }

    private int incrementKeyCounter() {
        return (this.keyCounter + 1) % this.readSets.size();
    }

}
