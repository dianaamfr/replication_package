package com.dissertation.referencearchitecture.compute.storage;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class ReaderStorage extends Storage {
    private ConcurrentMap<Integer, String> partitionsMaxTimestamp;
    private String stableTime;

    public ReaderStorage() {
        super();
        this.partitionsMaxTimestamp = new ConcurrentHashMap<>();
        this.stableTime = Utils.MIN_TIMESTAMP;
    }

    public void init(Set<Integer> partitions) {
        for (Integer partition : partitions) {
            this.partitionsMaxTimestamp.put(partition, Utils.MIN_TIMESTAMP);
        }
    }

    public void put(String key, String timestamp, ByteString value) {
        super.put(key, timestamp, value);
        this.setPartitionMaxTimestamp(Utils.getKeyPartitionId(key), timestamp);
    }

    public void setStableTime() {
        this.stableTime = Collections.min(this.partitionsMaxTimestamp.values());
    }

    public String getStableTime() {
        return this.stableTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.stableTime);
        builder.append(this.keyVersions.toString());
        return builder.toString();
    }

    public ConcurrentMap<Integer, String> getPartitionsMaxTimestamp() {
        return this.partitionsMaxTimestamp;
    }

    public void setPartitionMaxTimestamp(Integer partition, String timestamp) {
        this.partitionsMaxTimestamp.compute(partition, (k, v) -> {
            if (timestamp.compareTo(v) > 0) {
                return timestamp;
            }
            return v;
        });
    }

    public int getTotalVersions() {
        int size = 0;
        for (String key : this.keyVersions.keySet()) {
            size += this.keyVersions.get(key).getVersionChain(Utils.MIN_TIMESTAMP, this.stableTime).size();
        }
        return size;
    }
}
