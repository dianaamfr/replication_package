package com.dissertation.referencearchitecture;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;
import com.dissertation.referencearchitecture.compute.clock.ClockState.State;

public class TestClock {
    public static void main(String[] args) {
        HLC hlc = new HLC(new TimeProvider(new ScheduledThreadPoolExecutor(10), 10000));
        System.out.println("INITIAL = " + hlc.getCurrentTimestamp());
        ClockState write = new ClockState(System.currentTimeMillis()+60, 4, State.WRITE);
        System.out.println("RCV WRITE = " + write.toString());
        ClockState sync = new ClockState(System.currentTimeMillis()+20000, 9, State.SYNC);
        System.out.println("RCV SYNC = " + sync.toString());


        System.out.println("AFTER WRITE = " + hlc.writeEvent(write));
        ClockState currentTime = hlc.updateAndGetState();
        if(!currentTime.isSync()) {
            System.out.println(currentTime.getState());
            System.out.println("DON'T SYNC");
        } else {
            System.out.println("AFTER SYNC = " + hlc.syncEvent(sync));
        }
        
        currentTime = hlc.writeComplete();
        System.out.println(currentTime.getState());
        currentTime = hlc.updateAndGetState();
        if(!currentTime.isSync()) {
            System.out.println(currentTime.getState());
            System.out.println("DON'T SYNC");
        } else {
            System.out.println("AFTER SYNC = " + hlc.syncEvent(sync));

        }
        currentTime = hlc.updateAndGetState();
        if(!currentTime.isSync()) {
            System.out.println(currentTime.getState());
            System.out.println("DON'T SYNC");
        } else {
            System.out.println("AFTER SYNC = " + hlc.syncEvent(sync));
        }
        System.out.println("AFTER WRITE = " + hlc.writeEvent(write));
    }
}
