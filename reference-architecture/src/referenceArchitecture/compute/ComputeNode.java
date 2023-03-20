package referenceArchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class ComputeNode {
    protected ScheduledThreadPoolExecutor scheduler;
    protected final String id;

    public ComputeNode(ScheduledThreadPoolExecutor scheduler, String id) {
        this.scheduler = scheduler;
        this.id = id;
    }
}
