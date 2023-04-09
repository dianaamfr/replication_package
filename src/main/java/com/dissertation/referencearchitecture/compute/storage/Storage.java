package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
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

    public void delete(String key, String timestamp) {
        this.keyVersions.computeIfPresent(key, (k, v) -> {
            v.delete(timestamp);
            return v;
        });
    }

    public Entry<String, ByteString> get(String key, String maxTimestamp)
            throws KeyNotFoundException, KeyVersionNotFoundException {
        if (!this.keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return this.keyVersions.get(key).get(maxTimestamp);
    }

    public ConcurrentMap<String, VersionChain> getState() {
        return this.keyVersions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.keyVersions.toString());
        return builder.toString();
    }

}
