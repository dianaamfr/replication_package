package com.dissertation.referencearchitecture.compute.storage;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;

public class VersionChain implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeMap<String, Integer> versions;

    public VersionChain() {
        this.versions = new TreeMap<>();
    }

    public void put(String timestamp, Integer value) {
        versions.put(timestamp, value);
    }

    public Entry<String, Integer> get(String maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<String, Integer> entry = versions.floorEntry(maxTimestamp);
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

    public SortedMap<String, Integer> getVersionChain(String maxKey) {
        return this.versions.subMap("0.0", true, maxKey, true);
    }
}
