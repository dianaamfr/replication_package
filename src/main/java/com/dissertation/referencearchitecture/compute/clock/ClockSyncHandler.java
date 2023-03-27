package com.dissertation.referencearchitecture.compute.clock;

import org.json.JSONObject;

import com.dissertation.referencearchitecture.compute.storage.LogUtils;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class ClockSyncHandler implements Runnable {
    private LogicalClock logicalClock;
    private S3Helper s3Helper; 
    private long clockValue;
    private Storage storage;
    private String partition;

    public ClockSyncHandler(LogicalClock logicalClock, S3Helper s3Helper, Storage storage, String partition) {
        this.logicalClock = logicalClock;
        this.s3Helper = s3Helper;
        this.storage = storage;
        this.partition = partition;
    }

    @Override
    public void run() {
        syncClock();
        pushClock();
    }


    private void syncClock() {
        if(!this.logicalClock.hasNewUpdatesAndReset()) {
            this.clockValue = this.logicalClock.getClockValue();
            String higherClock = this.s3Helper.getClocksAfter(String.valueOf(this.clockValue));
            
            if(higherClock != null) {
                System.out.println("Sync clock: is " + this.clockValue + " received " + higherClock.split("Clock/")[1]);
                String receivedClock = higherClock.split("Clock/")[1];
                long newClock = this.logicalClock.sync(Long.valueOf(receivedClock));
    
                try {
                    JSONObject json = LogUtils.toJson(this.storage.getState(), newClock);
                    System.out.println(json.toString());
                    this.s3Helper.persistLog(this.partition, String.valueOf(newClock), json.toString());
                } catch(Exception e) {
                    System.err.println(String.format("Error: %s", e.getMessage()));
                }
            }
        }
    }

    private void pushClock() {
        this.s3Helper.persistClock(String.valueOf(this.logicalClock.getClockValue()));
    }
}
