package com.dissertation.referencearchitecture.compute.storage;

import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class WriterStorage extends Storage {
    private ConcurrentMap<String, JSONObject> jsonVersionChains;
    private ConcurrentMap<String, String> lastPushedVersions;

    public WriterStorage() {
        super();
        this.jsonVersionChains = new ConcurrentHashMap<>();
        this.lastPushedVersions = new ConcurrentHashMap<>();
    }

    public void setLastPushedVersion(String key, String version) {
        this.lastPushedVersions.put(key, version);
    }

    public String getLastPushedVersion(String key) {
        return this.lastPushedVersions.getOrDefault(key, Utils.MIN_TIMESTAMP);
    }

    public void updateJsonState(String timestamp) {
        for (Entry<String, VersionChain> versionChain : this.getState().entrySet()) {
            String lastPushedTimestamp = this.getLastPushedVersion(versionChain.getKey());
            SortedMap<String, ByteString> keySnapshotVersions = versionChain.getValue()
                    .getVersionChain(lastPushedTimestamp, timestamp);
            for (Entry<String, ByteString> version : keySnapshotVersions.entrySet()) {
                this.appendToJsonState(versionChain.getKey(), version.getKey(), version.getValue());
            }
        }
    }

    public JSONObject getJsonState() {
        JSONObject state = new JSONObject();
        JSONArray versionChains = new JSONArray();
        for (JSONObject jsonVersionChain : this.jsonVersionChains.values()) {
            versionChains.put(jsonVersionChain);
        }
        state.put(Utils.LOG_STATE, versionChains);
        return state;
    }

    private void appendToJsonState(String key, String timestamp, ByteString value) {
        if (!this.jsonVersionChains.containsKey(key)) {
            this.jsonVersionChains.put(key, this.getVersionChainJson(key));
        }
        this.jsonVersionChains.get(key).getJSONArray(Utils.LOG_VERSIONS).put(getVersionJson(timestamp, value));
        this.lastPushedVersions.put(key, timestamp);
    }

    private JSONObject getVersionChainJson(String key) {
        JSONObject keyVersionChainJson = new JSONObject();
        keyVersionChainJson.put(Utils.LOG_KEY, key);
        keyVersionChainJson.put(Utils.LOG_VERSIONS, new JSONArray());
        return keyVersionChainJson;
    }

    private JSONObject getVersionJson(String timestamp, ByteString value) {
        JSONObject keyVersion = new JSONObject();
        keyVersion.put(Utils.LOG_TIMESTAMP, timestamp);
        keyVersion.put(Utils.LOG_VALUE, Utils.stringFromByteString(value));
        return keyVersion;
    }
}
