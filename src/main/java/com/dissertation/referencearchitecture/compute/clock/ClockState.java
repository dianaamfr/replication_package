package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.utils.Utils;

public class ClockState {
    private long logicalTime;
    private long logicalCount;
    private State state;

    public enum State {
        WRITE,
        SYNC,
        INACTIVE,
        ACTIVE
    }

    public ClockState() {
        this.logicalTime = 0;
        this.logicalCount = 0;
        this.state = State.INACTIVE;
    }

    public ClockState(long logicalTime, long logicalCount, State state) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.state = state;
    }

    public void setLogicalTime(long logicalTime) {
        this.logicalTime = logicalTime;
    }

    public void setLogicalCount(long logicalCount) {
        this.logicalCount = logicalCount;
    }

    public void setState(State state) {
        this.state = state;
    }

    public long getLogicalTime() {
        return this.logicalTime;
    }

    public long getLogicalCount() {
        return this.logicalCount;
    }

    public boolean isActive() {
        return this.state.equals(State.ACTIVE);
    }

    public boolean isSync() {
        return this.state.equals(State.SYNC);
    }

    public boolean isWrite() {
        return this.state.equals(State.WRITE);
    }

    public boolean isInactive() {
        return this.state.equals(State.INACTIVE);
    }

    public boolean isZero() {
        return this.logicalTime == 0 && this.logicalCount == 0;
    }

    public static ClockState fromString(String timestamp, State state) throws InvalidTimestampException {
        String[] parts = timestamp.split("-");
        if (parts.length != 2) {
            throw new InvalidTimestampException();
        }
        return new ClockState(Long.valueOf(parts[0]), Long.valueOf(parts[1]), state);
    }

    @Override
    public String toString() {
        return String.format(Utils.TIMESTAMP_FORMAT, logicalTime, Utils.TIMESTAMP_SEPARATOR, logicalCount);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClockState)) {
            return false;
        }
        ClockState timestamp = (ClockState) o;
        return this.logicalTime == timestamp.getLogicalTime();
    }
}
