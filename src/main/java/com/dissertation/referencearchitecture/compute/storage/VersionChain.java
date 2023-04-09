package com.dissertation.referencearchitecture.compute.storage;

import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class VersionChain {
    private ConcurrentSkipListMap<String, ByteString> versions;

    public VersionChain() {
        this.versions = new ConcurrentSkipListMap<>();
    }

    public void put(String timestamp, ByteString value) {
        this.versions.put(timestamp, value);
    }

    public void delete(String timestamp) {
        if (this.versions.containsKey(timestamp)) {
            this.versions.remove(timestamp);
        }
    }

    public Entry<String, ByteString> get(String maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<String, ByteString> entry = this.versions.floorEntry(maxTimestamp);
            if (entry == null) {
                throw new KeyVersionNotFoundException();
            }
            return entry;
        } catch (Exception e) {
            throw new KeyVersionNotFoundException();
        }
    }

    @Override
    public String toString() {
        return this.versions.toString();
    }

    public SortedMap<String, ByteString> getVersionChain(String maxKey) {
        return this.versions.subMap(Utils.MIN_TIMESTAMP, true, maxKey, true);
    }
}
