package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

import com.dissertation.utils.Utils;

public class HLC {
    private AtomicReference<HLCState> state;
    private final TimeProvider timeProvider;

    private final BinaryOperator<HLCState> writeStartOperator;
    private final BinaryOperator<HLCState> writeEndOperator;
    private final BinaryOperator<HLCState> pushEndOperator;
    private final BinaryOperator<HLCState> syncClockOperator;

    public HLC(TimeProvider timeProvider) {
        this.state = new AtomicReference<>(new HLCState());
        this.timeProvider = timeProvider;

        this.writeStartOperator = this::startWriteOperation;
        this.writeEndOperator = this::endWriteOperation;
        this.pushEndOperator = this::pushEndOperation;
        this.syncClockOperator = this::syncClockOperation;
    }

    public HLCState startWrite(HLCState recvEvent) {
        return this.state.accumulateAndGet(recvEvent, this.writeStartOperator);
    }

    public HLCState endWrite(HLCState recvEvent) {
        return this.state.accumulateAndGet(recvEvent, this.writeEndOperator);
    }

    public HLCState syncClock(HLCState recvEvent) {
        return this.state.accumulateAndGet(recvEvent, this.syncClockOperator);
    }

    public HLCState endPush(HLCState recvEvent) {
        return this.state.accumulateAndGet(recvEvent, this.pushEndOperator);
    }

    public HLCState getState() {
        return this.state.get();
    }

    private HLCState startWriteOperation(HLCState prevState, HLCState recvState) {
        long logicalTime = Math.max(prevState.getLogicalTime(),
                Math.max(timeProvider.getTime(), recvState.getLogicalTime()));
        long logicalCount = 0;
        String lastWrite = prevState.getLastWrite(); // Preserve the timestamp of the last completed write

        boolean isLocalTimeEqual = logicalTime == prevState.getLogicalTime();
        boolean isRecvTimeEqual = logicalTime == recvState.getLogicalTime();

        if (isLocalTimeEqual && isRecvTimeEqual) {
            logicalCount = Math.max(prevState.getLogicalCount(), recvState.getLogicalCount()) + 1;
        } else if (isLocalTimeEqual) {
            logicalCount = prevState.getLogicalCount() + 1;
        } else if (isRecvTimeEqual) {
            logicalCount = recvState.getLogicalCount() + 1;
        }

        // Increment clock and set the write in progress flag
        return new HLCState(logicalTime, logicalCount, lastWrite, true);
    }

    private HLCState endWriteOperation(HLCState prevState, HLCState recvState) {
        // Update the last write ready to push and reset the write in progress flag
        return new HLCState(prevState.getLogicalTime(), prevState.getLogicalCount(), recvState.getLastWrite(), false);
    }

    private HLCState pushEndOperation(HLCState prevState, HLCState recvState) {
        if (prevState.getLastWrite().equals(recvState.getLastWrite())) {
            return new HLCState(prevState.getLogicalTime(), prevState.getLogicalCount(), Utils.MIN_TIMESTAMP, prevState.isWriteInProgress());
        }
        return prevState;
    }

    private HLCState syncClockOperation(HLCState prevState, HLCState recvState) {
        if (prevState.newWrites()) {
            return prevState;
        }

        long logicalTime = Math.max(prevState.getLogicalTime(), recvState.getLogicalTime());
        long logicalCount = 0;
        boolean isLocalTimeEqual = logicalTime == prevState.getLogicalTime();
        boolean isRecvTimeEqual = logicalTime == recvState.getLogicalTime();

        if (isLocalTimeEqual && isRecvTimeEqual) {
            logicalCount = Math.max(prevState.getLogicalCount(), recvState.getLogicalCount());
        } else if (isLocalTimeEqual) {
            logicalCount = prevState.getLogicalCount();
        } else if (isRecvTimeEqual) {
            logicalCount = recvState.getLogicalCount();
        }

        return new HLCState(logicalTime, logicalCount, prevState.getLastWrite(), prevState.isWriteInProgress());
    }
}
