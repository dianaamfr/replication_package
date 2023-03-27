package com.dissertation.referencearchitecture.compute.clock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;

public class LogicalClock {
    private AtomicLong clockValue;
    private AtomicBoolean newUpdates;
    private LongBinaryOperator maxIncrementOp = (x, y) -> Math.max(x, y) + 1;
    private LongBinaryOperator maxOp = (x, y) -> Math.max(x, y);

    public LogicalClock() {
        this.clockValue = new AtomicLong(0);
        this.newUpdates = new AtomicBoolean(false);
    }

    public long tick(long lastWriteTimestamp) {
        this.newUpdates.set(true);
        return this.clockValue.accumulateAndGet(lastWriteTimestamp, maxIncrementOp);
    }

    public long sync(long timestamp) {
        return this.clockValue.accumulateAndGet(timestamp, maxOp);
    }

    public boolean hasNewUpdatesAndReset() {
        return this.newUpdates.compareAndSet(true, false);
    }

    public long getClockValue() {
        return this.clockValue.get();
    }
}
    