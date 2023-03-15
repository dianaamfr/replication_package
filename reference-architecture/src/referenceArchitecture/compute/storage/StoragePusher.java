package referenceArchitecture.compute.storage;

import java.rmi.RemoteException;

import referenceArchitecture.compute.clock.LogicalClock;
import referenceArchitecture.datastore.DataStoreInterface;

public class StoragePusher extends StorageHandler {
    DataStoreInterface dataStoreStub;
    LogicalClock logicalClock;

    public StoragePusher(Storage storage, DataStoreInterface dataStoreStub, LogicalClock logicalClock) {
        super(storage);
        this.dataStoreStub = dataStoreStub;
        this.logicalClock = logicalClock;
    }

    @Override
    public void run() {
        this.push();
        this.storage.setStableTime();
    }

    private void push() {
        try {
            // TODO: change key to identify the correct region and partition
            // TODO: condition to only store when logical clock value has changed
            System.out.println(this.logicalClock.toString());
            this.dataStoreStub.write(this.logicalClock.toString(), this.storage.getState());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
