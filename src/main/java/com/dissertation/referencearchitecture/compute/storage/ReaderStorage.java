package com.dissertation.referencearchitecture.compute.storage;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class ReaderStorage extends Storage {
    private ConcurrentMap<String, String> partitionsMaxTimestamp;
    private String stableTime;
    private Region region;
    
    public ReaderStorage(Region region) {
        super();
        this.partitionsMaxTimestamp = new ConcurrentHashMap<>();
        this.stableTime = Utils.MIN_TIMESTAMP;
        this.region = region;
    }

    public void init() {
        Set<String> partitions = Config.getPartitions(this.region);
        for(String partition: partitions) {
            this.partitionsMaxTimestamp.put(partition, Utils.MIN_TIMESTAMP);
        }
    }

    public void put(String key, String timestamp, byte[] value) throws KeyNotFoundException {
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
