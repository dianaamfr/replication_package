package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.locks.ReentrantLock;

import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class ClockSyncHandler implements Runnable {
    private HLC hlc;
    private S3Helper s3Helper;
    private ReentrantLock mutex;   
    private StoragePusher storagePusher; 

    public ClockSyncHandler(HLC hlc, S3Helper s3Helper, StoragePusher storagePusher, ReentrantLock mutex) {
        this.hlc = hlc;
        this.s3Helper = s3Helper;
        this.storagePusher = storagePusher;
        this.mutex = mutex;
    }

    @Override
    public void run() {
        if(this.hlc.hasNewUpdates()) {
            return;
        }
        
        String clockValue = this.hlc.getTimestamp().toString();
        String recentClock = this.s3Helper.getClocksAfter(clockValue);
        if(recentClock == null) {
            return;
        }
        recentClock = recentClock.split("Clock/")[1];      

        System.out.println("Current clock " + clockValue + ", recv clock " + recentClock);
        HybridTimestamp recentTimestamp;
        try {
            recentTimestamp = HybridTimestamp.fromString(recentClock);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Error: Invalid recent timestamp"));
            return;
        }

        mutex.lock();
        try {
            this.hlc.syncEvent(recentTimestamp);
            String newTimestamp = this.hlc.getTimestamp().toString();
            this.s3Helper.persistClock(newTimestamp);
            boolean result = this.storagePusher.push(newTimestamp);
            if(result == false) {
                // TODO: reset timestamp (?)
            }
        } finally {
            mutex.unlock();
        }
    }

}
