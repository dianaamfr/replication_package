package referenceArchitecture.compute.clock;

public class LogicalClock {
    private long clock;
    private boolean stateSaved;

    public LogicalClock() {
        this.clock = 0;
        this.stateSaved = true;
    }

    public void tick(long lastWriteTimestamp) {
        this.clock = Math.max(this.clock, lastWriteTimestamp) + 1;
        this.stateSaved = false;
    }

    public long internalEvent(long lastWriteTimestamp) {
        return Math.max(this.clock, lastWriteTimestamp) + 1;
    }

    public void stateSaved() {
        this.stateSaved = true;
    }

    public boolean isStateSaved() {
        return this.stateSaved;
    }   

    @Override
    public String toString() {
        return String.valueOf(clock);
    }
    
}
