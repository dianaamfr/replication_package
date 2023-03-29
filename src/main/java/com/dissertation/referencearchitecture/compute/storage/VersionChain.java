package com.dissertation.referencearchitecture.compute.storage;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.utils.Utils;

public class VersionChain {
    private TreeMap<String, byte[]> versions;

    public VersionChain() {
        this.versions = new TreeMap<>();
    }

    public void put(String timestamp, byte[] value) {
        versions.put(timestamp, value);
    }

    public Entry<String, byte[]> get(String maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<String, byte[]> entry = versions.floorEntry(maxTimestamp);
            if(entry == null) {
                throw new KeyVersionNotFoundException();
            }
            return entry;
        } catch(Exception e) {
            throw new KeyVersionNotFoundException();
        }
    }

    @Override
    public String toString() {
        return versions.toString();
    }

    public SortedMap<String, byte[]> getVersionChain(String maxKey) {
        return this.versions.subMap(Utils.MIN_TIMESTAMP, true, maxKey, true);
    }
}
