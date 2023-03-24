package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dissertation.referencearchitecture.compute.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.compute.exceptions.KeyVersionNotFoundException;

public class Storage {
    protected ConcurrentMap<String, VersionChain> keyVersions;

    public Storage() {
        this.keyVersions = new ConcurrentHashMap<>();
    }

    public void put(String key, long timestamp, int value) throws KeyNotFoundException {
        if(!this.keyVersions.containsKey(key)){
            this.keyVersions.put(key, new VersionChain());
        }
        this.keyVersions.get(key).put(timestamp, value);
    }

    public Entry<Long, Integer> get(String key, long maxTimestamp) throws KeyNotFoundException, KeyVersionNotFoundException {
        if(!this.keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return this.keyVersions.get(key).get(maxTimestamp);
    }

    public ConcurrentMap<String,VersionChain> getState() {
        return this.keyVersions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.keyVersions.toString());
        return builder.toString();
    }
    
}
