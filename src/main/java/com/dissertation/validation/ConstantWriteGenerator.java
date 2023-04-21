package com.dissertation.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.WriteRequestLog;
import com.dissertation.validation.logs.WriteResponseLog;
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
    private final ArrayDeque<Log> logs;

    private static final String USAGE = "Usage: ConstantWriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <delay:Int> <totalWrites:Int> <keys:String>";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, long delay, int totalWrites, List<String> keys) {
        
        this.client = new Client(readAddress, writeAddresses);   
        this.delay = delay;
        this.totalWrites = totalWrites;
        this.keys = keys;
        this.payload = new AtomicInteger(Utils.PAYLOAD_START);
        
        this.scheduler = scheduler;
        this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS); 

        this.counter = new AtomicInteger(0);
        this.countDown = new CountDownLatch(totalWrites);
        this.logs = new ArrayDeque<>(this.totalWrites * 2);
        
        if(Utils.LOGS) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    Utils.logToFile(logs, String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));
                }
            });
        }
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
        this.client.shutdown();
    }

    private class WriteGeneratorRequest implements Runnable {

        @Override
        public void run() {
            int count = counter.getAndIncrement();
            String key = keys.get(count % keys.size());
            int partitionId = Utils.getKeyPartitionId(key);

            if (count < totalWrites) {
                ByteString value = Utils.byteStringFromString(String.valueOf(payload.getAndIncrement()));

                long t1 = System.currentTimeMillis();
                WriteResponse writeResponse = client.requestWrite(key, value);
                long t2 = System.currentTimeMillis();

                if(Utils.LOGS) {
                    logs.add(new WriteRequestLog(key, partitionId, t1));
                    logs.add(new WriteResponseLog(key, partitionId, writeResponse.getWriteTimestamp(), t2));
                }
                countDown.countDown();
            }
        }
    }
}
