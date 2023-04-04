package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.compute.clock.ClockState.State;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;

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
        ClockState currentTime = this.hlc.updateAndGetState();
        if(!currentTime.isSync()) {
            return;
        }
        
        ClockState currentTimestamp = this.hlc.getCurrentTimestamp();
        S3ReadResponse response = this.s3Helper.getClocksAfter(currentTimestamp.toString());
        if(!response.hasTimestamp()) {
            return;
        }
        //System.out.println("SYNC: current=" + currentTimestamp + " recv=" + recvTimestamp);     

        ClockState recvTime;
        try {
            recvTime = ClockState.fromString(response.getTimestamp(), State.SYNC);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Error: Invalid recent timestamp"));
            return;
        }
        // Try to sync clock with received clock
        ClockState newTime = this.hlc.syncEvent(recvTime);
        if(!newTime.isSync()) {
            return;
        } 

        String newTimestamp = newTime.toString();
        this.s3Helper.persistClock(newTimestamp);
        this.storagePusher.push(newTimestamp);
        this.hlc.syncComplete();
    }

}
