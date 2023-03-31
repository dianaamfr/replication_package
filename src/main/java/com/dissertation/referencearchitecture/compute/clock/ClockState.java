package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;

public class ClockState {
    private long logicalTime;      
    private long logicalCount;
    private Event originEvent;
    private WriteState writeState;

    public enum Event {
        SYNC,
        WRITE
    }

    public enum WriteState {
        IDLE,
        IN_PROGRESS
    }

    public ClockState() {
        this.logicalTime = 0;
        this.logicalCount = 0;
        this.originEvent = Event.WRITE;
        this.writeState = WriteState.IDLE;
    }

    public ClockState(long logicalTime, long logicalCount) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.originEvent = Event.WRITE;
        this.writeState = WriteState.IDLE;
    }

    public ClockState(long logicalTime, long logicalCount, Event originEvent) {
        this(logicalTime, logicalCount);
        this.originEvent = originEvent;
    }

    public ClockState(long logicalTime, long logicalCount, Event originEvent, WriteState writeState) {
        this(logicalTime, logicalCount, originEvent);
        this.writeState = writeState;
    }

    public void setLogicalTime(long logicalTime) {
        this.logicalTime = logicalTime;
    }

    public void setLogicalCount(long logicalCount) {
        this.logicalCount = logicalCount;
    }

    public void setOriginEvent(Event originEvent) {
        this.originEvent = originEvent;
    }

    public void setWriteState(WriteState writeState) {
        this.writeState = writeState;
    }

    public long getLogicalTime() {
        return this.logicalTime;
    }

    public long getLogicalCount() {
        return this.logicalCount;
    }

    public Event getOriginEvent() {
        return this.originEvent;
    }

    public boolean isWriteEvent() {
        return this.originEvent.equals(Event.WRITE);
    }

    public WriteState getWriteState() {
        return this.writeState;
    }

    public boolean isWriteInProgress() {
        return this.writeState.equals(WriteState.IN_PROGRESS);
    }

    public static ClockState fromString(String timestamp) throws InvalidTimestampException {
        String[] parts = timestamp.split("\\.");  
        if(parts.length != 2) {
            throw new InvalidTimestampException();
        }
        return new ClockState(Long.valueOf(parts[0]), Long.valueOf(parts[1]));
    }

    @Override
    public String toString() {
        return logicalTime + "." + logicalCount;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ClockState)) {
            return false;
        }
        ClockState timestamp = (ClockState) o;
        return this.logicalTime == timestamp.getLogicalTime();
    }
}
