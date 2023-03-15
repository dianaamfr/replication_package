package referenceArchitecture.compute.storage;

public class StoragePusher extends StorageHandler {

    public StoragePusher(Storage storage) {
        super(storage);
    }

    @Override
    public void run() {
        this.push();
        this.storage.setStableTime();
    }

    private void push() {
        // TODO: push into ECDS
    }
}
