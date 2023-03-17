package referenceArchitecture.compute.clock;

public class LogicalClock {
    private long clock;
    private boolean stateSaved;

    public LogicalClock() {
        this.clock = 0;
        this.stateSaved = true;
    }

    public void tick() {
        this.clock++;
        this.stateSaved = false;
    }

    public long internalEvent() {
        return this.clock + 1;
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
