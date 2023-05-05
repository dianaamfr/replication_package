package com.dissertation.validation;

import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.s3.S3Response;
import com.dissertation.eventual.utils.Utils;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.WriteRequestLog;
import com.dissertation.validation.logs.WriteResponseLog;

public class ConstantWriteGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private Client client;
    private final long delay;
    private int totalWrites;
    private final List<String> keys;
    private long payload;
    private int counter;
    private CountDownLatch countDown;
    private final ArrayDeque<Log> logs;

    private static final String USAGE = "Usage: ConstantWriteGenerator <delay:Int> <totalWrites:Int> <key:String>+";

    public ConstantWriteGenerator(ScheduledThreadPoolExecutor scheduler, long delay, int totalWrites, List<String> keys) throws URISyntaxException {
        this.client = new Client();
        this.delay = delay;
        this.totalWrites = totalWrites;
        this.keys = keys;
        this.payload = Utils.PAYLOAD_START_LONG;

        this.counter = 0;
        this.countDown = new CountDownLatch(totalWrites);
        this.logs = new ArrayDeque<>(this.totalWrites * 2);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Utils.logToFile(logs,
                        String.format("%s-%s", Utils.WRITE_CLIENT_ID, Utils.getCurrentRegion().toString()));
            }
        });

        this.scheduler = scheduler;
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        List<String> keys = new ArrayList<>();

        if (args.length < 3) {
            System.err.println(USAGE);
            return;
        }

        try {
            long delay = Long.parseLong(args[0]);
            int totalWrites = Integer.parseInt(args[1]);
            for (int i = 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantWriteGenerator writer = new ConstantWriteGenerator(scheduler, delay, totalWrites, keys);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    public void run() {
        this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS);

        try {
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WriteGeneratorRequest implements Runnable {

        @Override
        public void run() {
            String key = keys.get(counter % keys.size());
            int partitionId = Utils.getKeyPartitionId(key); 

            if (counter < totalWrites) {
                long t1 = System.currentTimeMillis();
                S3Response writeResponse = client.write(key, String.valueOf(payload));
                long t2 = System.currentTimeMillis();

                logs.add(new WriteRequestLog(payload, partitionId, t1));
                logs.add(new WriteResponseLog(payload, partitionId, t2));

                counter++;
                payload++;
                countDown.countDown();
            }
        }
    }
}
