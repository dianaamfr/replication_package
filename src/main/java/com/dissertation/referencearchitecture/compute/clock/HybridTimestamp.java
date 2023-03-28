package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;

public class HybridTimestamp {
    private long logicalTime;      
    private long logicalCount;
    private long physicalTime;

    public HybridTimestamp(long physicalTime) {
        this.logicalTime = 0;
        this.logicalCount = 0;
        this.physicalTime = physicalTime;
    }

    public HybridTimestamp(long logicalTime, long logicalCount) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.physicalTime = 0;
    }

    public void setLogicalTime(long logicalTime) {
        this.logicalTime = logicalTime;
    }

    public void setLogicalCount(long logicalCount) {
        this.logicalCount = logicalCount;
    }

    public long getLogicalTime() {
        return this.logicalTime;
    }

    public long getLogicalCount() {
        return this.logicalCount;
    }

    public long getPhysicalTime() {
        return this.physicalTime;
    }

    public static HybridTimestamp fromString(String timestamp) throws InvalidTimestampException {
        String[] parts = timestamp.split("\\.");  
        if(parts.length != 2) {
            throw new InvalidTimestampException();
        }
        return new HybridTimestamp(Long.valueOf(parts[0]), Long.valueOf(parts[1]));
    }

    @Override
    public String toString() {
        return logicalTime + "." + logicalCount;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof HybridTimestamp)) {
            return false;
        }
        HybridTimestamp timestamp = (HybridTimestamp) o;
        return this.logicalTime == timestamp.getLogicalTime();
    }

    
}
