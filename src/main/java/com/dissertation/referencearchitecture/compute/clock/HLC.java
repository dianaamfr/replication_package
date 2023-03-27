package com.dissertation.referencearchitecture.compute.clock;

public class HLC {
    private TimeProvider timeProvider;
    private HybridTimestamp currentTime;

    public HLC(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.currentTime = new HybridTimestamp(timeProvider.getTime());
    }

    public void localEvent() {
        long physicalTime = timeProvider.getTime();
        HybridTimestamp newTime = new HybridTimestamp(physicalTime);
        newTime.setLogicalTime(
            Math.max(this.currentTime.getLogicalTime(), 
            physicalTime));
        
        if(newTime.equals(this.currentTime)) {
            newTime.setLogicalCount(this.currentTime.getLogicalCount() + 1);
        }

        this.currentTime = newTime;
    }

    public void externalEvent(HybridTimestamp recvTime) {
        long physicalTime = timeProvider.getTime();
        HybridTimestamp newTime = new HybridTimestamp(physicalTime);
        newTime.setLogicalTime(
            Math.max(this.currentTime.getLogicalTime(), 
            Math.max(physicalTime, recvTime.getLogicalTime())));
       
        boolean isLocalTimeEqual = newTime.equals(this.currentTime);
        boolean isRecvTimeEqual = newTime.equals(recvTime);
        if(isLocalTimeEqual && isRecvTimeEqual) {
            newTime.setLogicalCount(Math.max(
                this.currentTime.getLogicalCount(), 
                recvTime.getLogicalCount()));
        }

        this.currentTime = newTime;
    }

    public HybridTimestamp getTimestamp() {
        return this.currentTime;
    }

}
