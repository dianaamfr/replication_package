package com.dissertation.referencearchitecture.compute.clock;

public class TestProvider implements TimeProvider {

    @Override
    public long getTime() {
        return System.nanoTime();
    }
    
}
