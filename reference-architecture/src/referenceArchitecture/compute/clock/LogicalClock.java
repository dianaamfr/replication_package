package referenceArchitecture.compute.clock;

public class LogicalClock {
    private long clock;

    public LogicalClock() {
        this.clock = 0;
    }

    private void tick() {
        this.clock++;
    }

    public long internalEvent() {
        tick();
        return this.clock;
    }

    @Override
    public String toString() {
        return String.valueOf(clock);
    }
    
}
