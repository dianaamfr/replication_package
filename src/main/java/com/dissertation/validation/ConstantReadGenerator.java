package com.dissertation.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.GoodputLog;
import com.google.protobuf.ByteString;

public class ConstantReadGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private final Client client;
    private final long delay;
    private final int totalReads;
    private final Set<String> keys;
    private int counter;
    private CountDownLatch countDown;
    private long lastPayload;
    private long startTime;
    private long endTime;

    private static final String USAGE = "Usage: ConstantReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <totalReads:Int> <keys:String>";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int totalReads, Set<String> keys) {
        this.client = new Client(readAddress, writeAddresses);
        this.delay = delay;
        this.totalReads = totalReads;
        this.keys = keys;

        this.counter = 0;
        this.countDown = new CountDownLatch(totalReads);
        this.lastPayload = 0;

        this.scheduler = scheduler;
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();
        Set<String> keys = new HashSet<>();

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
            int totalReads = Integer.parseInt(args[addressesEndIndex + 1]);
            for (int i = addressesEndIndex + 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, readAddress, writeAddresses, delay,
                    totalReads, keys);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void run() {
        this.scheduler.scheduleWithFixedDelay(new ReadGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS);

        try {
            this.countDown.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.scheduler.shutdown();
        this.client.shutdown();
    }

    private class ReadGeneratorRequest implements Runnable {
        @Override
        public void run() {
            if (counter < totalReads) {
                if(counter == 0) {
                    startTime = System.currentTimeMillis();
                }

                ROTResponse rotResponse = client.requestROT(keys);
                endTime = System.currentTimeMillis();

                for (ByteString value : rotResponse.getValuesMap().values()) {
                    String valueStr = Utils.stringFromByteString(value);
                    if (valueStr.isBlank()) {
                        continue;
                    }

                    try {
                        long valueLong = Long.valueOf(valueStr);
                        if (valueLong > lastPayload) {
                            lastPayload = valueLong;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if((counter + 1) % totalReads == 0) {
                    System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG : 0, endTime - startTime));
                }

                countDown.countDown();
                counter++;
            }
        }
    }

}
