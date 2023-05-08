package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.utils.Utils;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class HLCState {
    private final long logicalTime;
    private final long logicalCount;
    private final String lastWrite;

    public HLCState() {
        this.logicalTime = 0;
        this.logicalCount = 0;
        this.lastWrite = "";
    }

    public HLCState(long logicalTime, long logicalCount) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.lastWrite = Utils.MIN_TIMESTAMP;
    }

    public HLCState(long logicalTime, long logicalCount, String lastWrite) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.lastWrite = lastWrite;
    }

    public long getLogicalTime() {
        return this.logicalTime;
    }

    public long getLogicalCount() {
        return this.logicalCount;
    }

    public String getLastWrite() {
        return this.lastWrite;
    }

    public boolean areNewWritesAvailable() {
        return !this.lastWrite.equals(Utils.MIN_TIMESTAMP);
    }

    public boolean noWritesOccurred() {
        return this.lastWrite.isBlank();
    }

    public static HLCState fromRecvTimestamp(String timestamp) throws StatusRuntimeException {
        String[] parts = timestamp.split(Utils.TIMESTAMP_SEPARATOR);
        if (parts.length != 2) {
            Status status = Status.INVALID_ARGUMENT.withDescription("Invalid timestamp format");
            throw status.asRuntimeException();
        }
        return new HLCState(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

    public static HLCState fromLastWriteTimestamp(String lastStoredTime) {
        return new HLCState(0, 0, lastStoredTime);
    }

    @Override
    public String toString() {
        return String.format(Utils.TIMESTAMP_FORMAT, this.logicalTime, Utils.TIMESTAMP_SEPARATOR, this.logicalCount);
    }

}
