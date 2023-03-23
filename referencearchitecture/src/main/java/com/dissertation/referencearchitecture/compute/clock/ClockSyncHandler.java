package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.s3.S3Helper;

public class ClockSyncHandler implements Runnable {
    private LogicalClock logicalClock;
    private S3Helper s3Helper;

    public ClockSyncHandler(LogicalClock logicalClock, S3Helper s3Helper) {
        this.logicalClock = logicalClock;
        this.s3Helper = s3Helper;
    }

    @Override
    public void run() {
        // String lastClock = this.s3Helper.getClocksAfter(this.logicalClock.toString());
        // if(lastClock != null) {
        //     System.out.println("MINE: " + this.logicalClock.toString());
        //     System.out.println("S3: " + lastClock.split("Clock/")[1]);
        //     this.logicalClock.sync(Long.valueOf(lastClock.split("Clock/")[1]));
        // }
        // this.s3Helper.persistClock(this.logicalClock.toString());
    }

}
