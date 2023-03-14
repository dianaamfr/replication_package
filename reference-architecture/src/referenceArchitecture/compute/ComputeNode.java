package referenceArchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import referenceArchitecture.compute.storage.Storage;

public abstract class ComputeNode {
    protected Storage storage;
    protected ScheduledThreadPoolExecutor scheduler;

    public ComputeNode(Storage storage, ScheduledThreadPoolExecutor scheduler) {
        this.storage = storage;
        this.scheduler = scheduler;
    }
}
