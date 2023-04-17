package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import com.dissertation.referencearchitecture.compute.clock.ClockState.State;

public class HLC {
    private final TimeProvider timeProvider;
    private final BinaryOperator<ClockState> updateClockOperator;
    private final UnaryOperator<ClockState> syncClockOperator;
    private final UnaryOperator<ClockState> writeCompleteOperator;
    private final UnaryOperator<ClockState> syncCompleteOperator;
    private AtomicReference<ClockState> currentTime;
    private AtomicReference<ClockState> safePushTime;

    public HLC(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.updateClockOperator = this::updateClock;
        this.writeCompleteOperator = this::setWriteComplete;
        this.syncClockOperator = this::syncClock;
        this.syncCompleteOperator = this::setSyncComplete;
        this.currentTime = new AtomicReference<>(new ClockState());
        this.safePushTime = new AtomicReference<>(new ClockState());
    }

    public ClockState writeEvent(ClockState recvEvent) {
        return this.currentTime.accumulateAndGet(recvEvent, this.updateClockOperator);
    }

    public ClockState writeComplete() {
        return this.currentTime.updateAndGet(this.writeCompleteOperator);
    }

    public ClockState syncClock() {
        return this.currentTime.updateAndGet(this.syncClockOperator);
    }

    public ClockState syncComplete() {
        return this.currentTime.updateAndGet(this.syncCompleteOperator);
    }

    public void setSafePushTime(ClockState safeTime) {
        this.safePushTime.set(safeTime);
    }

    public ClockState getAndResetSafePushTime() {
        return this.safePushTime.getAndSet(new ClockState());
    }

    private ClockState updateClock(ClockState prevTime, ClockState recvTime) {
        ClockState newTime = new ClockState();

        newTime.setLogicalTime(
                Math.max(prevTime.getLogicalTime(),
                        Math.max(timeProvider.getTime(), recvTime.getLogicalTime())));

        boolean isLocalTimeEqual = newTime.equals(prevTime);
        boolean isRecvTimeEqual = newTime.equals(recvTime);

        if (isLocalTimeEqual && isRecvTimeEqual) {
            newTime.setLogicalCount(Math.max(prevTime.getLogicalCount(), recvTime.getLogicalCount())
                    + 1);
        } else if (isLocalTimeEqual) {
            newTime.setLogicalCount(prevTime.getLogicalCount() + 1);
        } else if (isRecvTimeEqual) {
            newTime.setLogicalCount(recvTime.getLogicalCount() + 1);
        }

        newTime.setState(State.WRITE);

        return newTime;
    };

    private ClockState setWriteComplete(ClockState prevTime) {
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.ACTIVE);
    }

    private ClockState syncClock(ClockState prevTime) {
        if (prevTime.isWrite()) {
            return prevTime;
        }
        if (prevTime.isActive()) {
            return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.INACTIVE);
        }
        return new ClockState(Math.max(prevTime.getLogicalTime(), timeProvider.getTime()), prevTime.getLogicalCount(), State.SYNC);
    }

    private ClockState setSyncComplete(ClockState prevTime) {
        if (prevTime.isWrite()) {
            return prevTime;
        }
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), State.INACTIVE);
    }
}
