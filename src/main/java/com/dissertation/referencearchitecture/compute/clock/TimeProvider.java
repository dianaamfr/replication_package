package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimeProvider {
    private final ScheduledThreadPoolExecutor scheduler;
    private long currentTime;

    public TimeProvider(ScheduledThreadPoolExecutor scheduler, long delay) {
        this.scheduler = scheduler;
        this.currentTime = 0;

        final TimeRetriever timeRetriever = new TimeRetriever();
        this.scheduler.scheduleWithFixedDelay(timeRetriever, 0, delay, TimeUnit.MILLISECONDS);
    }

    private class TimeRetriever implements Runnable {
        @Override
        public void run() {
            currentTime = System.currentTimeMillis();
        }
    }

    public long getTime() {
        return this.currentTime;
    }

}
