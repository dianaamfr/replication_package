package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

public class StoragePusher {
    private Storage storage;
    private S3Helper s3Helper;
    private String partition;

    public StoragePusher(Storage storage, S3Helper s3Helper, String partition) {
        this.storage = storage;
        this.partition = partition;
        this.s3Helper = s3Helper;
    }

    public boolean push(String timestamp) {
        try {
            JSONObject json = toJson(this.storage.getState(), timestamp);
            //System.out.println(json);
            this.s3Helper.persistClock(timestamp);
            return this.s3Helper.persistLog(this.partition, timestamp, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject toJson(ConcurrentMap<String, VersionChain> state, String timestamp) {
        JSONObject stateJson = new JSONObject();
        JSONArray versionChainsJson = new JSONArray();

        // For each key
        for(Entry<String, VersionChain> versionChain: state.entrySet()) {
            JSONObject versionChainJson = new JSONObject(); // Version chain of the key
            JSONArray versionsJson = new JSONArray(); // Array of versions of a key
            // For each version of the key that belongs to the snapshot defined by clockValue
            for(Entry<String, byte[]> version: versionChain.getValue().getVersionChain(timestamp).entrySet()) {
                JSONObject versionJson = new JSONObject();
                versionJson.put("key", version.getKey());
                versionJson.put("value", Utils.stringFromByteArray(version.getValue()));
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
