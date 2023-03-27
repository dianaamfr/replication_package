package com.dissertation.referencearchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.SystemTimeProvider;
import com.dissertation.referencearchitecture.compute.clock.Timestamp;

public class TestClock {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        SystemTimeProvider provider = new SystemTimeProvider(scheduler, 10000);
        HLC hlc = new HLC(provider);
        System.out.println(hlc.getTimestamp());
        hlc.localEvent();
        System.out.println(hlc.getTimestamp());
        hlc.localEvent();
        System.out.println(hlc.getTimestamp());
        hlc.localEvent();
        System.out.println(hlc.getTimestamp());

        long aux = System.nanoTime();
        Timestamp externalEvent = new Timestamp(aux);
        externalEvent.setLogicalTime(aux + 20);
        hlc.receiveEvent(externalEvent);
        System.out.println(hlc.getTimestamp());
    }
}
