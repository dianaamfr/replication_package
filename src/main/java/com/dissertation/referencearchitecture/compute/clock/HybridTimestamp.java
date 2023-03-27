package com.dissertation.referencearchitecture.compute.clock;

public class HybridTimestamp {
    private long logicalTime;      
    private long logicalCount;
    private long physicalTime;

    public HybridTimestamp(long physicalTime) {
        this.logicalTime = 0;
        this.logicalCount = 0;
        this.physicalTime = physicalTime;
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

    @Override
    public String toString() {
        return "Timestamp [logicalTime=" + logicalTime + ", logicalCount=" + logicalCount + ", physicalTime="
                + physicalTime + "]";
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
