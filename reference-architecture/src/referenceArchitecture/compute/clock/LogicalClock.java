package referenceArchitecture.compute.clock;

public class LogicalClock {
    private long clock;

    public LogicalClock() {
        this.clock = 0;
    }

    public void tick() {
        this.clock++;
    }

    public long internalEvent() {
        return this.clock + 1;
    }

    @Override
    public String toString() {
        return String.valueOf(clock);
    }
    
}
