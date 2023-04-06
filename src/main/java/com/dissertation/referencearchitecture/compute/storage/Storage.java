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

    public void put(String key, String timestamp, ByteString value) throws KeyNotFoundException {
        if(!this.keyVersions.containsKey(key)){
            this.keyVersions.put(key, new VersionChain());
        }
        this.keyVersions.get(key).put(timestamp, value);
    }

    public Entry<String, ByteString> get(String key, String maxTimestamp) throws KeyNotFoundException, KeyVersionNotFoundException {
        if(!this.keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return this.keyVersions.get(key).get(maxTimestamp);
    }

    public void delete(String key, String timestamp) throws KeyNotFoundException {
        if(this.keyVersions.containsKey(key)){
            this.keyVersions.get(key).delete(timestamp);
        }
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
