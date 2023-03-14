package referenceArchitecture.compute.storage;

public class StorageUpdater implements Runnable {
    private Storage storage;

    public StorageUpdater(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        storage.setStableTime();
    }
    
}
