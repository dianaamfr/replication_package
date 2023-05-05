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
    private final int totalReads;
    private final List<String> keys;
    private int counter;
    private CountDownLatch countDown;
    private long lastPayload;
    private long startTime;
    private long endTime;

    private static final String USAGE = "Usage: ConstantReadGenerator <delay:Int> <totalReads:Int> <key:String>+";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, long delay, int totalReads, List<String> keys) throws URISyntaxException {
        this.client = new Client();
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
        List<String> keys = new ArrayList<>();

        if (args.length != 3) {
            System.err.println(USAGE);
            return;
        }

        try {
            long delay = Long.parseLong(args[0]);
            int totalReads = Integer.parseInt(args[1]);
            for (int i = 2; i < args.length; i++) {
                keys.add(args[i]);
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, delay, totalReads, keys);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    private void run() {
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
            if (counter < totalReads) {
                if (counter == 0) {
                    startTime = System.currentTimeMillis();
                }
                
                String key = keys.get(counter % keys.size());
                S3ReadResponse response = client.read(key);
                endTime = System.currentTimeMillis();

                if (response.getContent().isBlank()) {
                    return;
                }

                try {
                    long valueLong = Long.parseLong(response.getContent());
                    if (valueLong > lastPayload) {
                        lastPayload = valueLong;
                    }
                } catch (NumberFormatException e) {
                    return;
                }

                if ((counter + 1) % totalReads == 0) {
                    System.out.println(new GoodputLog(lastPayload > 0 ? lastPayload - Utils.PAYLOAD_START_LONG : 0,
                            endTime - startTime).toJson().toString());
                }

                countDown.countDown();
                counter++;
            }
        }
    }

}
