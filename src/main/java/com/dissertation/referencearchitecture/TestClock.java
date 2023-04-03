package com.dissertation.referencearchitecture;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;

public class TestClock {
    public static void main(String[] args) {
        // HLC hlc = new HLC(new TimeProvider(new ScheduledThreadPoolExecutor(10), 10000));
        // System.out.println(hlc.getCurrentTimestamp());
        // ClockState write = new ClockState(System.currentTimeMillis()+60, 4);
        // System.out.println(write.toString());
        // ClockState sync = new ClockState(System.currentTimeMillis()-200, 4, Event.SYNC);
        // System.out.println(sync.toString());
        // System.out.println(hlc.writeEvent(write));

        // hlc.getLastClockAndSetSyncEvent().isWriteEvent();

        // System.out.println(hlc.syncEvent(sync));
    }
}
