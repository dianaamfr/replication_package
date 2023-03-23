package com.dissertation.referencearchitecture.compute.clock;

public class LogicalClock {
    private long clockValue;
    private long lastStateSaved;

    public LogicalClock() {
        this.clockValue = 0;
        this.lastStateSaved = 0;
    }

    public long nextClockValue(long lastWriteTimestamp) {
        return Math.max(this.clockValue, lastWriteTimestamp) + 1;
    }

    public void tick(long lastWriteTimestamp) {
        this.clockValue = Math.max(this.clockValue, lastWriteTimestamp) + 1;
    }

    // public void sync(long timestamp) {
    //     this.clock = Math.max(this.clock, timestamp);
    //     this.stateSaved = false;
    // }

    public void setLastStateSaved(long timestamp) {
        this.lastStateSaved = timestamp;
    }

    public boolean isStateSaved(long timestamp) {
        return timestamp <= this.lastStateSaved;
    } 

    public long getClockValue() {
        return this.clockValue;
    }

    @Override
    public String toString() {
        return String.valueOf(this.clockValue);
    }
    
}
