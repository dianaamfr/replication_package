package com.dissertation.referencearchitecture.compute;

import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.TestProvider;
import com.dissertation.referencearchitecture.compute.clock.Timestamp;

public class TestClock {
    public static void main(String[] args) {
        TestProvider provider = new TestProvider();
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
