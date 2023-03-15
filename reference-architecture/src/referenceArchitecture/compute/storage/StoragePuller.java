package referenceArchitecture.compute.storage;

public class StoragePuller extends StorageHandler {

    public StoragePuller(Storage storage) {
        super(storage);
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
    }

    private void pull() {
        // TODO: pull from ECDS

        
        storage.put(null, 0, 0);
    }
    
}
