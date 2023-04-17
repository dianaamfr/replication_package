package com.dissertation.referencearchitecture.compute.storage;

import java.util.ArrayDeque;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.ClockState.State;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.S3OperationLog;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.Log.NodeType;
import com.google.protobuf.ByteString;

public class StoragePusher implements Runnable {
    private HLC hlc;
    private Storage storage;
    private S3Helper s3Helper;
    private int partition;
    private String id;
    private ArrayDeque<Log> logs;
    private final String region;

    public StoragePusher(HLC hlc, Storage storage, S3Helper s3Helper, int partition, String id, ArrayDeque<Log> logs, String region) {
        this.hlc = hlc;
        this.storage = storage;
        this.s3Helper = s3Helper;
        this.partition = partition;
        this.id = id;
        this.logs = logs;
        this.region = region;
    }

    @Override
    public void run() {
        ClockState currentTime = this.hlc.trySyncAndGetClock();
        // Sync in the absence of writes
        if (currentTime.isSync()) {
            this.sync(currentTime);
            return;
        }
         
        // Push if new writes have been made
        ClockState safePushTime = this.hlc.getAndResetSafePushTime();
        if(!safePushTime.isZero()) {
            this.push(safePushTime.toString());

            if(Utils.VALIDATION_LOGS) {
                this.logs.add(new S3OperationLog(NodeType.WRITER, this.id, safePushTime.toString(), this.partition, true));
            }
        }
    }

    private void sync(ClockState currentTime) {
        S3ReadResponse response = this.s3Helper.getClocksAfter(currentTime.toString());
        if (!response.hasTimestamp()) {
            return;
        }
        // System.out.println("SYNC: current=" + currentTimestamp + " recv=" +
        // response.getTimestamp());

        // Get the most recent timestamp
        ClockState recvTime;
        try {
            recvTime = ClockState.fromString(response.getTimestamp(), State.SYNC);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Invalid recent timestamp"));
            return;
        }

        // Try to sync clock
        ClockState newTime = this.hlc.syncEvent(recvTime);

        // If a write occurred, abort sync
        if (!newTime.isSync()) {
            return;
        }

        // Push log with new timestamp
        String newTimestamp = newTime.toString();

        this.push(newTimestamp);
        this.hlc.syncComplete();
    }

    private boolean push(String timestamp) {
        try {
            JSONObject json = this.toJson(this.storage.getState(), timestamp);
            // System.out.println(json);
            this.s3Helper.persistClock(timestamp);
            return this.s3Helper.persistLog(Utils.getPartitionBucket(this.partition, this.region), timestamp, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject toJson(ConcurrentMap<String, VersionChain> state, String timestamp) {
        JSONObject stateJson = new JSONObject();
        JSONArray versionChainsJson = new JSONArray();

        for (Entry<String, VersionChain> versionChain : state.entrySet()) {
            JSONArray versionsJson = new JSONArray();
            SortedMap<String, ByteString> keySnapshotVersions = versionChain.getValue().getVersionChain(timestamp);
            for (Entry<String, ByteString> version : keySnapshotVersions.entrySet()) {
                versionsJson.put(getVersionJson(version.getKey(), version.getValue()));
            }
            versionChainsJson.put(getVersionChainJson(versionChain.getKey(), versionsJson));
        }
        stateJson.put(Utils.LOG_STATE, versionChainsJson);
        return stateJson;
    }

    private JSONObject getVersionChainJson(String key, JSONArray versions) {
        JSONObject keyVersionChainJson = new JSONObject();
        keyVersionChainJson.put(Utils.LOG_KEY, key);
        keyVersionChainJson.put(Utils.LOG_VERSIONS, versions);
        return keyVersionChainJson;
    }

    private JSONObject getVersionJson(String timestamp, ByteString value) {
        JSONObject keyVersion = new JSONObject();
        keyVersion.put(Utils.LOG_TIMESTAMP, timestamp);
        keyVersion.put(Utils.LOG_VALUE, Utils.stringFromByteString(value));
        return keyVersion;
    }
}
