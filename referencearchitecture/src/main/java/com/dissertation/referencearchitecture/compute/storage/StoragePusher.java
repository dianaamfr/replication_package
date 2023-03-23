package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.compute.clock.LogicalClock;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class StoragePusher implements Runnable {
    private Storage storage;
    private LogicalClock logicalClock;
    private S3Helper s3Helper;
    private String partition;

    public StoragePusher(Storage storage, LogicalClock logicalClock, S3Helper s3Helper, String partition) {
        this.storage = storage;
        this.logicalClock = logicalClock;
        this.partition = partition;
        this.s3Helper = s3Helper;
    }

    @Override
    public void run() {
        this.push();
    }

    private void push() {
        if(this.logicalClock.isStateSaved()) {
            return;
        };

        try {
            JSONObject json = toJson(this.storage.getState());
            System.out.println(json.toString());
            this.s3Helper.persistLog(this.partition, this.logicalClock.toString(), json.toString());
            this.logicalClock.stateSaved();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject toJson(ConcurrentMap<String, VersionChain> state) {
        JSONObject stateJson = new JSONObject();
        JSONArray versionChainsJson = new JSONArray();

        // For each key
        for(Entry<String, VersionChain> versionChain: state.entrySet()) {
            JSONObject versionChainJson = new JSONObject(); // Version chain of the key
            JSONArray versionsJson = new JSONArray(); // Array of versions of a key
            // For each version in the key
            for(Entry<Long, Integer> version: versionChain.getValue().getVersionChain().entrySet()) {
                JSONObject versionJson = new JSONObject();
                versionJson.put("key", version.getKey().toString());
                versionJson.put("value", version.getValue());
                versionsJson.put(versionJson);
            }
            versionChainJson.put("key", versionChain.getKey());
            versionChainJson.put("value", versionsJson);
            versionChainsJson.put(versionChainJson);
        }

        stateJson.put("state", versionChainsJson);
        return stateJson;
    }
}
