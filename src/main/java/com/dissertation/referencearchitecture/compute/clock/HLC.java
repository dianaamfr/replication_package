package com.dissertation.referencearchitecture.compute.clock;

public class HLC {
    private TimeProvider timeProvider;
    private Timestamp currentTime;

    public HLC(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        long physicalTime = timeProvider.getTime();
        this.currentTime = new Timestamp(physicalTime);
    }

    public void localEvent() {
        long physicalTime = timeProvider.getTime();
        Timestamp newTime = new Timestamp(physicalTime);
        newTime.setLogicalTime(
            Math.max(this.currentTime.getLogicalTime(), 
            physicalTime));
        
        if(newTime.equals(this.currentTime)) {
            newTime.setLogicalCount(this.currentTime.getLogicalCount() + 1);
        }

        this.currentTime = newTime;
    }

    public void receiveEvent(Timestamp recvTime) {
        long physicalTime = timeProvider.getTime();
        Timestamp newTime = new Timestamp(physicalTime);
        newTime.setLogicalTime(
            Math.max(this.currentTime.getLogicalTime(), 
            Math.max(physicalTime, recvTime.getLogicalTime())));
       
        // TODO: study the possibility of not advancing the clock
        boolean isLocalTimeEqual = newTime.equals(this.currentTime);
        boolean isRecvTimeEqual = newTime.equals(recvTime);
        if(isLocalTimeEqual && isRecvTimeEqual) {
            long maxCount = Math.max(this.currentTime.getLogicalCount(), recvTime.getLogicalCount());
            newTime.setLogicalCount(maxCount + 1);
        } else if(isLocalTimeEqual) {
            newTime.setLogicalCount(this.currentTime.getLogicalCount() + 1);
        } else if(isRecvTimeEqual) {
            newTime.setLogicalCount(recvTime.getLogicalCount() + 1);
        }

        this.currentTime = newTime;
    }

    public String getTimestamp() {
        return this.currentTime.getLogicalTime() + " " + this.currentTime.getLogicalCount();
    }

}
