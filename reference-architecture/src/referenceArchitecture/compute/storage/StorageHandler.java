package referenceArchitecture.compute.storage;

public abstract class StorageHandler implements Runnable {
    protected Storage storage;

    public StorageHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    abstract public void run();
    
}
