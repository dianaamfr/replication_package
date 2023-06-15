package com.dissertation.referencearchitecture.compute.clock;

import com.dissertation.utils.Utils;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class HLCState {
    private final long logicalTime;
    private final long logicalCount;
    private final boolean writeInProgress;
    private final String lastWrite;

    public HLCState(long logicalTime, long logicalCount) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.writeInProgress = false;
        this.lastWrite = Utils.MIN_TIMESTAMP;
    }

    public HLCState() {
        this(0,0);
    }

    public HLCState(long logicalTime, long logicalCount, String lastWrite, boolean writeInProgress) {
        this.logicalTime = logicalTime;
        this.logicalCount = logicalCount;
        this.writeInProgress = writeInProgress;
        this.lastWrite = lastWrite;
    }

    public long getLogicalTime() {
        return this.logicalTime;
    }

    public long getLogicalCount() {
        return this.logicalCount;
    }

    public boolean isWriteInProgress() {
        return this.writeInProgress;
    }

    public String getLastWrite() {
        return this.lastWrite;
    }

    public boolean newWrites() {
        return this.newWritesToPush() || this.writeInProgress;
    }

    public boolean newWritesToPush() {
        return !this.lastWrite.equals(Utils.MIN_TIMESTAMP);
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
        return new HLCState(0, 0, lastStoredTime, false);
    }

    @Override
    public String toString() {
        return String.format(Utils.TIMESTAMP_FORMAT, this.logicalTime, Utils.TIMESTAMP_SEPARATOR, this.logicalCount);
    }

}
