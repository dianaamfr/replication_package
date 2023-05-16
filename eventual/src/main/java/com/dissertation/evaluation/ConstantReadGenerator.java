package com.dissertation.evaluation;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.s3.S3ReadResponse;
import com.dissertation.eventual.utils.Utils;
import com.dissertation.evaluation.logs.GoodputLog;

public class ConstantReadGenerator {
    private final ScheduledThreadPoolExecutor scheduler;
    private final Client client;
    private final long delay;
    private final int readTime;
    private final List<String> keys;
    private final CountDownLatch countDown;
    private int keyCounter;
    private long lastPayload;
    private long startTime;
    private long endTime;

    private static final String USAGE = "Usage: ConstantReadGenerator <delay:Int> <readTime:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, long delay, int readTime, List<String> keys)
            throws URISyntaxException {
        this.scheduler = scheduler;
        this.client = new Client();
        this.delay = delay;
        this.readTime = readTime;
        this.keys = keys;
        this.countDown = new CountDownLatch(1);
        this.keyCounter = 0;
        this.lastPayload = 0;
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
            int readTime = Integer.parseInt(args[1]);
            for (int i = 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, delay, readTime, keys);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    private void run() {
        this.startTime = System.currentTimeMillis();
        this.scheduler.scheduleWithFixedDelay(new ReadGeneratorRequest(), 0, this.delay, TimeUnit.MILLISECONDS);

        try {
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        @Override
        public void run() {
            if (countDown.getCount() > 0) {
                String key = keys.get(keyCounter % keys.size());
                S3ReadResponse response = client.read(key);
                endTime = System.currentTimeMillis();

                if (response.getContent().isBlank()) {
                    keyCounter = incrementKeyCounter();
                    return;
                }

                long valueLong = Long.parseLong(response.getContent());
                if (valueLong > lastPayload) {
                    lastPayload = valueLong;
                }
                keyCounter = incrementKeyCounter();
            }

            if (endTime - startTime >= readTime) {
                countDown.countDown();
                System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG + 1 : 0,
                        endTime - startTime).toJson().toString());
            }
        }
    }

    private int incrementKeyCounter() {
        return (this.keyCounter + 1) % this.keys.size();
    }

}
