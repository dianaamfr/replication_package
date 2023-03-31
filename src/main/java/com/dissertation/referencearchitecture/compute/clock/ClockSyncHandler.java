package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.compute.clock.ClockState.Event;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class ClockSyncHandler implements Runnable {
    private HLC hlc;
    private S3Helper s3Helper;
    private StoragePusher storagePusher; 

    public ClockSyncHandler(HLC hlc, S3Helper s3Helper, StoragePusher storagePusher) {
        this.hlc = hlc;
        this.s3Helper = s3Helper;
        this.storagePusher = storagePusher;
    }

    @Override
    public void run() {
        // Cancel sync if new writes have been performed
        if(this.hlc.getLastClockAndSetSyncEvent().isWriteEvent()) {
            return;
        }
        
        ClockState currentTimestamp = this.hlc.getCurrentTimestamp();
        String recvTimestamp = this.s3Helper.getClocksAfter(currentTimestamp.toString());
        if(recvTimestamp == null) {
            return;
        }
        recvTimestamp = recvTimestamp.split("Clock/")[1]; 
        //System.out.println("SYNC: current=" + currentTimestamp + " recv=" + recvTimestamp);     

        ClockState recvTime;
        try {
            recvTime = ClockState.fromString(recvTimestamp);
            recvTime.setOriginEvent(Event.SYNC);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Error: Invalid recent timestamp"));
            return;
        }

        // Try to sync clock with received clock
        ClockState newTime = this.hlc.syncEvent(recvTime);
        if(newTime.toString().compareTo(recvTime.toString()) < 0) {
            return;
        } 

        String newTimestamp = newTime.toString();
        this.s3Helper.persistClock(newTimestamp);
        boolean result = this.storagePusher.push(newTimestamp);
        if(result == false) {
            // TODO: reset timestamp (?)
        }
    }

}
