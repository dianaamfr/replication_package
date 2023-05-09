package com.dissertation.validation;

import java.util.ArrayList;
import java.util.HashSet;
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
    private ScheduledThreadPoolExecutor scheduler;
    private final Client client;
    private final long delay;
    private final int keysPerRead;
    private final List<Set<String>> readSets;
    private int keyCounter;
    private CountDownLatch countDown;
    private long lastPayload;
    private long startTime;
    private long endTime;
    private boolean exit;

    private static final String USAGE = "Usage: ConstantReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <keysPerRead:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int keysPerRead, List<String> keys) {
        this.client = new Client(readAddress, writeAddresses);
        this.delay = delay;
        this.keysPerRead = keysPerRead;
        this.readSets = getReadSets(keys);
        this.keyCounter = 0;
        this.countDown = new CountDownLatch(1);
        this.lastPayload = 0;
        this.exit = false;

        this.scheduler = scheduler;
    }

    private List<Set<String>> getReadSets(List<String> keys) {
        List<Set<String>> readSets = new ArrayList<>();
        for (int i = 0; i < keys.size(); i += this.keysPerRead) {
            readSets.add(new HashSet<String>(keys.subList(i, i + this.keysPerRead)));
        }
        return readSets;
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
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 1]);
            for (int i = addressesEndIndex + 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            if(keys.size() % keysPerRead != 0) {
                System.err.println("The number of keys must be divisible by the keys per read.");
                return;
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, readAddress, writeAddresses, delay,
                    keysPerRead, keys);
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
            if (!exit) {
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
                    }
                }


                if (endTime - startTime >= Utils.GOODPUT_TIME) {
                    System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG : 0,
                    endTime - startTime).toJson().toString());
                    countDown.countDown();
                    exit = true;
                }

                keyCounter = incrementKeyCounter();
            }
        }
    }


    private int incrementKeyCounter() {
        return (this.keyCounter + 1) % this.readSets.size();
    }

}
