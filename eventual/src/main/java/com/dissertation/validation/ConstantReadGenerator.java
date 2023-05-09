package com.dissertation.validation;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.s3.S3ReadResponse;
import com.dissertation.eventual.utils.Utils;
import com.dissertation.validation.logs.GoodputLog;

public class ConstantReadGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private final Client client;
    private final long delay;
    private final List<String> keys;
    private int keyCounter;
    private CountDownLatch countDown;
    private long lastPayload;
    private long startTime;
    private long endTime;
    private boolean exit;

    private static final String USAGE = "Usage: ConstantReadGenerator <delay:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, long delay, List<String> keys) throws URISyntaxException {
        this.client = new Client();
        this.delay = delay;
        this.keys = keys;
        this.keyCounter = 0;
        this.countDown = new CountDownLatch(1);
        this.lastPayload = 0;
        this.exit = false;

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
            for (int i = 1; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, delay, keys);
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
            if (!exit) {
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
        return (this.keyCounter + 1) % this.keys.size();
    }

}
