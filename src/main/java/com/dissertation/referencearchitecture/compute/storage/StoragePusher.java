package com.dissertation.referencearchitecture.compute.storage;

import java.util.SortedMap;
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
            // System.out.println(json);
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

        for (Entry<String, VersionChain> versionChain : state.entrySet()) {
            JSONArray versionsJson = new JSONArray();
            SortedMap<String, byte[]> keySnapshotVersions = versionChain.getValue().getVersionChain(timestamp);
            for (Entry<String, byte[]> version : keySnapshotVersions.entrySet()) {
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

    private JSONObject getVersionJson(String timestamp, byte[] value) {
        JSONObject keyVersion = new JSONObject();
        keyVersion.put(Utils.LOG_TIMESTAMP, timestamp);
        keyVersion.put(Utils.LOG_VALUE, Utils.stringFromByteArray(value));
        return keyVersion;
    }
}
