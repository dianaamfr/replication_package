package com.dissertation.referencearchitecture.compute.storage;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dissertation.referencearchitecture.compute.clock.HLCState;
import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.S3OperationLog;
import com.dissertation.referencearchitecture.StableTimeRequest;
import com.dissertation.referencearchitecture.StableTimeResponse;
import com.dissertation.referencearchitecture.StableTimeServiceGrpc;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;
import com.dissertation.utils.Utils;

import io.grpc.StatusRuntimeException;

public class StoragePusher implements Runnable {
    private final S3Helper s3Helper;
    private final WriterStorage storage;
    private final HLC hlc;
    private final int partition;
    private final String region;
    private final List<StableTimeServiceGrpc.StableTimeServiceBlockingStub> stableTimeStubs;
    private AtomicInteger counter;
    private ArrayDeque<Log> logs;

    public StoragePusher(S3Helper s3Helper, WriterStorage storage, HLC hlc, int partition, String region, List<StableTimeServiceGrpc.StableTimeServiceBlockingStub> stableTimeStubs, AtomicInteger counter, ArrayDeque<Log> logs) {
        this.s3Helper = s3Helper;
        this.storage = storage;
        this.hlc = hlc;
        this.partition = partition;
        this.region = region;
        this.stableTimeStubs = stableTimeStubs;
        this.counter = counter;
        this.logs = logs;
    }

    @Override
    public void run() {
        // Checkpoint
        if(Utils.CHECKPOINTING && this.counter.decrementAndGet() == 0) {
            this.counter.set(Utils.CHECKPOINT_FREQUENCY);
            this.computeCheckpoint();
        }

        HLCState currentTime = this.hlc.getState();
        // Sync in the absence of writes
        if (!currentTime.newWrites()) {
            this.sync(currentTime);
            return;
        } 
        
        // Push new writes
        if(currentTime.newWritesToPush()) {
            String pushTimestamp = currentTime.getLastWrite();
            this.push(pushTimestamp);
            this.hlc.endPush(HLCState.fromLastWriteTimestamp(pushTimestamp));

            if (Utils.VISIBILITY_LOGS) {
                this.logs.add(new S3OperationLog(pushTimestamp, this.partition, true));
            }
        }
    }

    private void computeCheckpoint() {
        // Get minimum stable time
        String minStableTime = "";
        for(StableTimeServiceGrpc.StableTimeServiceBlockingStub stub : this.stableTimeStubs) {
            StableTimeResponse response = stub.stableTime(StableTimeRequest.newBuilder().build());
            if(minStableTime.isBlank()) {
                minStableTime = response.getStableTime();
            } else if(minStableTime.compareTo(response.getStableTime()) > 0) {
                minStableTime = response.getStableTime();
            }
        }
        
        // Perform checkpoint
        if(!minStableTime.isBlank() && minStableTime.compareTo(Utils.MIN_TIMESTAMP) > 0) {
            this.storage.pruneState(minStableTime);
        }
    }
    
    private void sync(HLCState currentTime) {
        S3ReadResponse response = this.s3Helper.getClocksAfter(currentTime.toString());
        if (!response.hasTimestamp()) {
            return;
        }

        // Get the most recent timestamp
        HLCState recvTime;
        try {
            recvTime = HLCState.fromRecvTimestamp(response.getTimestamp());
        } catch (StatusRuntimeException e) {
            System.err.println(String.format("Invalid recent timestamp"));
            return;
        }

        // Try to sync clock
        HLCState newTime = this.hlc.syncClock(recvTime);

        // If a write occurred, abort sync
        if (newTime.newWrites()) {
            return;
        }

        // Push log with new timestamp
        String newTimestamp = newTime.toString();
        this.push(newTimestamp);
    }

    private void push(String timestamp) {
        try {
            this.storage.updateJsonState(timestamp);
            this.s3Helper.persistLog(Utils.getPartitionBucket(this.partition, this.region), timestamp,
                    this.storage.getJsonState().toString());
            this.s3Helper.persistClock(timestamp);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

}
