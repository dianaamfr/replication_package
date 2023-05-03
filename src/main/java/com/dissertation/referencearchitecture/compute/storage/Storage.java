package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class Storage {
    protected ConcurrentMap<String, VersionChain> keyVersions;

    public Storage() {
        this.keyVersions = new ConcurrentHashMap<>();
    }

    public void put(String key, String timestamp, ByteString value) {
        this.keyVersions.compute(key, (k, v) -> {
            if (v == null) {
                v = new VersionChain();
            }
            v.put(timestamp, value);
            return v;
        });
    }

    public Entry<String, ByteString> get(String key, String maxTimestamp) {
        if (!this.keyVersions.containsKey(key)) {
            return Map.entry(Utils.MIN_TIMESTAMP, ByteString.EMPTY);
        }
        return this.keyVersions.get(key).get(maxTimestamp);
    }

    public ConcurrentMap<String, VersionChain> getState() {
        return this.keyVersions;
    }

    public void pruneState(String stableTime) {
        for (String key: this.keyVersions.keySet()) {
            this.keyVersions.compute(key, (k, v) -> {
                final String pruneEndKey = v.getPruneEndKey(stableTime);
                v.prune(pruneEndKey);
                return v;
            });
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.keyVersions.toString());
        return builder.toString();
    }
}
