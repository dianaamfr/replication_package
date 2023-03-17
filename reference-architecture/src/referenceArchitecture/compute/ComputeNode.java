package referenceArchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import referenceArchitecture.compute.storage.Storage;

public abstract class ComputeNode {
    protected Storage storage;
    protected ScheduledThreadPoolExecutor scheduler;
    protected final String id;
    protected final String region;

    public ComputeNode(Storage storage, ScheduledThreadPoolExecutor scheduler, String id, String region) {
        this.storage = storage;
        this.scheduler = scheduler;
        this.id = id;
        this.region = region;
    }
}
