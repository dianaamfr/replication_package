package com.dissertation.referencearchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.SystemTimeProvider;

public class TestClock {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        SystemTimeProvider provider1 = new SystemTimeProvider(scheduler, 15000);
        SystemTimeProvider provider2 = new SystemTimeProvider(scheduler, 5000);
        HLC hlc1 = new HLC(provider1);
        HLC hlc2 = new HLC(provider2);

        hlc1.localEvent();
        System.out.println(hlc1.getTimestamp());
        hlc1.localEvent();
        System.out.println(hlc1.getTimestamp());
        hlc1.localEvent();
        System.out.println(hlc1.getTimestamp());

        System.out.println();

        hlc2.localEvent();
        System.out.println(hlc2.getTimestamp());
        hlc2.localEvent();
        System.out.println(hlc2.getTimestamp());
        hlc2.localEvent();
        System.out.println(hlc2.getTimestamp());
        hlc2.localEvent();
        System.out.println(hlc2.getTimestamp());
        hlc2.localEvent();
        System.out.println(hlc2.getTimestamp());

        System.out.println();
        
        hlc1.externalEvent(hlc2.getTimestamp());
        System.out.println(hlc1.getTimestamp());
    }
}
