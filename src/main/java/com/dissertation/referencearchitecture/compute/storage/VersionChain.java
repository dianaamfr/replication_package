package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

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

    public String getPruneEndKey(String stableTime) {
        String v = this.versions.floorKey(stableTime);
        return v == null ? Utils.MIN_TIMESTAMP : v;
    }

    public void prune(String pruneEndKey) {
        this.versions.headMap(pruneEndKey, false).clear();
    }

    public Entry<String, ByteString> get(String maxTimestamp) {
        Entry<String, ByteString> entry = this.versions.floorEntry(maxTimestamp);
        if (entry == null) {
            entry = Map.entry(Utils.MIN_TIMESTAMP, ByteString.EMPTY);
        }
        return entry;
    }

    public Entry<String, ByteString> getLastVersion() {
        Entry<String, ByteString> lastVersion = this.versions.lastEntry();
        if(lastVersion == null) {
            lastVersion = Map.entry(Utils.MIN_TIMESTAMP, ByteString.EMPTY);
        }
        return lastVersion;
    }

    @Override
    public String toString() {
        return this.versions.toString();
    }

    public SortedMap<String, ByteString> getVersionChain(String minKey, String maxKey) {
        return this.versions.subMap(minKey, false, maxKey, true);
    }
}
