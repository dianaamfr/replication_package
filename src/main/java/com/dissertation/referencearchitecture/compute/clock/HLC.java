package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import com.dissertation.referencearchitecture.compute.clock.ClockState.Event;
import com.dissertation.referencearchitecture.compute.clock.ClockState.WriteState;

public class HLC {
    private TimeProvider timeProvider;
    private AtomicReference<ClockState> currentTime;
    private BinaryOperator<ClockState> updateClockOperator;
    private BinaryOperator<ClockState> syncClockOperator;
    private UnaryOperator<ClockState> originEventOperator;
    private UnaryOperator<ClockState> writeStateOperator;

    public HLC(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.currentTime = new AtomicReference<ClockState>(new ClockState());
        this.updateClockOperator = this::updateClock;
        this.syncClockOperator = this::syncClock;
        this.originEventOperator = this::setSyncEvent;
        this.writeStateOperator = this::setWriteComplete;
    }

    public ClockState getCurrentTimestamp() {
        return this.currentTime.get();
    }

   
    public ClockState writeEvent(ClockState recvEvent) {
        return this.currentTime.accumulateAndGet(recvEvent, this.updateClockOperator);
    }

    public ClockState syncEvent(ClockState recvEvent) {
        return this.currentTime.accumulateAndGet(recvEvent, this.syncClockOperator);
    }

    public ClockState getLastClockAndSetSyncEvent() {
        return this.currentTime.getAndUpdate(this.originEventOperator);
    }
 
    public void writeComplete() {
        this.currentTime.updateAndGet(this.writeStateOperator);
    }

    
    private ClockState updateClock(ClockState prevTime, ClockState recvTime) {
        ClockState newTime = new ClockState();
        long inc = recvTime.isWriteEvent() ? 1 : 0;

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

        newTime.setOriginEvent(recvTime.getOriginEvent());
        newTime.setWriteState(WriteState.IN_PROGRESS);

        return newTime;
    };

    private ClockState syncClock(ClockState prevTime, ClockState recvTime) {
        if (prevTime.isWriteEvent() || prevTime.isWriteInProgress()) {
            return prevTime;
        } 
        return updateClock(prevTime, recvTime);
    }

    private ClockState setWriteComplete(ClockState prevTime) {
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), Event.WRITE, WriteState.IDLE);
    }

    private ClockState setSyncEvent(ClockState prevTime) {
        return new ClockState(prevTime.getLogicalTime(), prevTime.getLogicalCount(), Event.SYNC);
    }
}
