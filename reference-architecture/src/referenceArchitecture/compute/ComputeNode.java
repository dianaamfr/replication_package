package referenceArchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import referenceArchitecture.compute.storage.Storage;

public abstract class ComputeNode {
    protected Storage storage;
    protected ScheduledThreadPoolExecutor scheduler;
    private final String id;

    public ComputeNode(Storage storage, ScheduledThreadPoolExecutor scheduler, String id) {
        this.storage = storage;
        this.scheduler = scheduler;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
