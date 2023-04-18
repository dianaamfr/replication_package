package com.dissertation.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;


public class ConstantWriteGenerator {
    protected ScheduledThreadPoolExecutor scheduler;
    private Client client;
    private final long delay;
    private int totalWrites;
    private final List<String> keys;
    private AtomicInteger payload;
    private AtomicInteger counter;
    private CountDownLatch countDown;

    private static final String USAGE = "Usage: WriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <totalWrites:Int> <keys:String>";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, long delay, int totalWrites, List<String> keys) {
        
        this.client = new Client(readAddress, writeAddresses, String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));   
        this.delay = delay;
        this.totalWrites = totalWrites;
        this.keys = keys;
        this.payload = new AtomicInteger(Utils.PAYLOAD_START);
        
        this.scheduler = scheduler;
        this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS); 

        this.counter = new AtomicInteger(0);
        this.countDown = new CountDownLatch(totalWrites);
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

            if(args.length < addressesEndIndex + 3) {
                System.err.println(USAGE);
                return;
            }

            long delay = Long.parseLong(args[addressesEndIndex]);
            int totalWrites = Integer.parseInt(args[addressesEndIndex + 1]);
            for (int i = addressesEndIndex + 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantWriteGenerator writer = new ConstantWriteGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, totalWrites, keys);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    public void run() {
        try {
            this.countDown.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.scheduler.shutdown();
    }

    private class WriteGeneratorRequest implements Runnable {

        @Override
        public void run() {
            int count = counter.getAndIncrement();
            String key = keys.get(count % keys.size());
            if (count < totalWrites) {
                ByteString value = Utils.byteStringFromString(String.valueOf(payload.getAndIncrement()));
                client.requestWrite(key, value);
                countDown.countDown();
            }
        }
    }
}
