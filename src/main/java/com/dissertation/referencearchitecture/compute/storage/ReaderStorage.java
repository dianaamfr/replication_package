package com.dissertation.referencearchitecture.compute.storage;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;

public class ReaderStorage extends Storage {
    private ConcurrentMap<String, String> partitionsMaxTimestamp;
    private String stableTime;
    private String region;
    
    public ReaderStorage(String region) {
        super();
        this.partitionsMaxTimestamp = new ConcurrentHashMap<>();
        this.stableTime = "0.0";
        this.region = region;
    }

    public void init() {
        Set<String> partitions = Config.getPartitions(this.region);
        for(String partition: partitions) {
            this.partitionsMaxTimestamp.put(partition, "0.0");
        }
    }

    public void put(String key, String timestamp, int value) throws KeyNotFoundException {
        super.put(key, timestamp, value);
        String partition = Config.getKeyPartition(this.region, key);
        if(timestamp.compareTo(partitionsMaxTimestamp.get(partition)) > 0) {
            this.partitionsMaxTimestamp.put(partition, timestamp);
        }
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

    public ConcurrentMap<String, String> getPartitionsMaxTimestamp() {
        return this.partitionsMaxTimestamp;
    }

    public void setPartitionMaxTimestamp(String partition, String timestamp) {
        if(timestamp.compareTo(this.partitionsMaxTimestamp.get(partition)) > 0) {
            this.partitionsMaxTimestamp.put(partition, timestamp);
        }
    }

}
