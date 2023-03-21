package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.compute.clock.LogicalClock;
import com.dissertation.referencearchitecture.datastore.DataStoreInterface;

public class StoragePusher implements Runnable {
    Storage storage;
    DataStoreInterface dataStoreStub;
    LogicalClock logicalClock;
    Integer partition;

    public StoragePusher(Storage storage, DataStoreInterface dataStoreStub, LogicalClock logicalClock, Integer partition) {
        this.dataStoreStub = dataStoreStub;
        this.storage = storage;
        this.logicalClock = logicalClock;
        this.partition = partition;
    }

    public void run() {
        this.push();
    }

    private void push() {
        if(this.logicalClock.isStateSaved()) {
            return;
        };

        try {
            JSONObject json = toJson(this.storage.getState());
            this.dataStoreStub.write(this.logicalClock.toString(), json.toString(), this.partition);
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
