package referenceArchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class ComputeNode {
    protected ScheduledThreadPoolExecutor scheduler;
    protected final String id;
    protected final String region;

    public ComputeNode(ScheduledThreadPoolExecutor scheduler, String id, String region) {
        this.scheduler = scheduler;
        this.id = id;
        this.region = region;
    }
}
