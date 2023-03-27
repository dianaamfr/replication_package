package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SystemTimeProvider implements TimeProvider {
    private ScheduledThreadPoolExecutor scheduler;
    private long currentTime;

    public SystemTimeProvider(ScheduledThreadPoolExecutor scheduler, long delay) {
        this.scheduler = scheduler;
        this.currentTime = 0;
        
        TimeRetriever timeRetriever = new TimeRetriever();
        this.scheduler.scheduleWithFixedDelay(timeRetriever, 0, delay, TimeUnit.MILLISECONDS);
    }

    private class TimeRetriever implements Runnable {
        @Override
        public void run() {
            currentTime = System.nanoTime(); 
        }
    }

    @Override
    public long getTime() {
        return this.currentTime;
    }
    
}
