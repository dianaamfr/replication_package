package referenceArchitecture.compute.storage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.config.Config;

public class ReaderStorage extends Storage {
    private ConcurrentMap<Integer, Long> partitionsMaxTimestamp;
    private long stableTime;
    private String region;
    
    public ReaderStorage(String region) {
        super();
        this.partitionsMaxTimestamp = new ConcurrentHashMap<>();
        this.stableTime = 0;
        this.region = region;
    }

    public void init() {
        List<Integer> partitions = Config.getPartitions(this.region);
        for(Integer partition: partitions) {
            this.partitionsMaxTimestamp.put(partition, 0L);
            super.init(Config.getKeys(partition));
        }
    }

    public void put(String key, long timestamp, int value) throws KeyNotFoundException {
        super.put(key, timestamp, value);
        Integer partition = Config.getKeyPartition(this.region, key);
        if(timestamp > partitionsMaxTimestamp.get(partition)) {
            this.partitionsMaxTimestamp.put(partition, timestamp);
        }
    }

    public void setStableTime() {
        this.stableTime = Collections.min(this.partitionsMaxTimestamp.values());
    }

    public long getStableTime() {
        return this.stableTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.stableTime);
        builder.append(this.keyVersions.toString());
        return builder.toString();
    }

    public ConcurrentMap<Integer, Long> getPartitionsMaxTimestamp() {
        return this.partitionsMaxTimestamp;
    }

    public void setPartitionMaxTimestamp(Integer partition, long timestamp) {
        if(timestamp > this.partitionsMaxTimestamp.get(partition)) {
            this.partitionsMaxTimestamp.put(partition, timestamp);
        }
    }

}
