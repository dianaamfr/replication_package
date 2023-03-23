package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.locks.ReentrantLock;

import com.dissertation.referencearchitecture.s3.S3Helper;

public class ClockSyncHandler implements Runnable {
    private LogicalClock logicalClock;
    private S3Helper s3Helper;
    private ReentrantLock mutex;    
    private long clockValue;

    public ClockSyncHandler(LogicalClock logicalClock, S3Helper s3Helper, ReentrantLock mutex) {
        this.logicalClock = logicalClock;
        this.s3Helper = s3Helper;
        this.mutex = mutex;
    }

    @Override
    public void run() {
        this.clockValue = this.logicalClock.getClockValue();
        String lastClock = this.s3Helper.getClocksAfter(String.valueOf(this.clockValue));
        if(lastClock != null) {
            System.out.println("Sync clock: is " + this.clockValue + " received " + lastClock.split("Clock/")[1]);

            mutex.lock();
            try {
                this.logicalClock.sync(Long.valueOf(lastClock.split("Clock/")[1]));
                lastClock = String.valueOf(this.logicalClock);
            } finally {
                mutex.unlock();
            }
        }

        this.s3Helper.persistClock(String.valueOf(this.logicalClock.getLastStateSaved()));
    }

}
