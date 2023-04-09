package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import com.dissertation.referencearchitecture.compute.clock.ClockState.State;

public class HLC {
    private final TimeProvider timeProvider;
    private final BinaryOperator<ClockState> updateClockOperator;
    private final BinaryOperator<ClockState> syncClockOperator;
    private final UnaryOperator<ClockState> requestSyncOperator;
    private final UnaryOperator<ClockState> writeCompleteOperator;
    private final UnaryOperator<ClockState> syncCompleteOperator;
    private AtomicReference<ClockState> currentTime;

    public HLC(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.updateClockOperator = this::updateClock;
        this.syncClockOperator = this::syncClock;
        this.requestSyncOperator = this::setSyncEvent;
        this.writeCompleteOperator = this::setWriteComplete;
        this.syncCompleteOperator = this::setSyncComplete;
        this.currentTime = new AtomicReference<ClockState>(new ClockState());
    }

    public ClockState writeEvent(ClockState recvEvent) {
        return this.currentTime.accumulateAndGet(recvEvent, this.updateClockOperator);
    }

    public ClockState writeComplete() {
        return this.currentTime.updateAndGet(this.writeCompleteOperator);
    }

    public ClockState trySyncAndGetClock() {
        return this.currentTime.updateAndGet(this.requestSyncOperator);
    }

    public ClockState syncEvent(ClockState recvEvent) {
        return this.currentTime.accumulateAndGet(recvEvent, this.syncClockOperator);
    }

    public ClockState syncComplete() {
        return this.currentTime.updateAndGet(this.syncCompleteOperator);
    }

    private ClockState updateClock(ClockState prevTime, ClockState recvTime) {
        ClockState newTime = new ClockState();
        long inc = recvTime.isWrite() ? 1 : 0;

        newTime.setLogicalTime(
                Math.max(prevTime.getLogicalTime(),
                        Math.max(timeProvider.getTime(), recvTime.getLogicalTime())));

        boolean isLocalTimeEqual = newTime.equals(prevTime);
        boolean isRecvTimeEqual = newTime.equals(recvTime);

        if (isLocalTimeEqual && isRecvTimeEqual) {
            newTime.setLogicalCount(Math.max(prevTime.getLogicalCount(), recvTime.getLogicalCount())
                    + inc);
        } else if (isLocalTimeEqual) {
            newTime.setLogicalCount(prevTime.getLogicalCount() + inc);
        } else if (isRecvTimeEqual) {
            newTime.setLogicalCount(recvTime.getLogicalCount() + inc);
        }

        newTime.setState(recvTime.isWrite() ? State.WRITE : State.SYNC);

        return newTime;
    };

    private ClockState setWriteComplete(ClockState prevTime) {
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.ACTIVE);
    }

    private ClockState setSyncEvent(ClockState prevTime) {
        if (prevTime.isWrite()) {
            return prevTime;
        }
        if (prevTime.isActive()) {
            return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.INACTIVE);
        }
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.SYNC);
    }

    private ClockState syncClock(ClockState prevTime, ClockState recvTime) {
        if (prevTime.isWrite()) {
            return prevTime;
        }

        if (prevTime.isActive()) {
            return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.INACTIVE);
        }

        return updateClock(prevTime, recvTime);
    }

    private ClockState setSyncComplete(ClockState prevTime) {
        if (prevTime.isWrite()) {
            return prevTime;
        }
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.INACTIVE);
    }
}
