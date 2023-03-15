package referenceArchitecture.compute.storage;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

import referenceArchitecture.datastore.DataStoreInterface;

public class StoragePuller extends StorageHandler {
    DataStoreInterface dataStoreStub;

    public StoragePuller(Storage storage, DataStoreInterface dataStoreStub) {
        super(storage);
        this.dataStoreStub = dataStoreStub;
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
    }

    private void pull() {
        try {
            Object obj = this.dataStoreStub.read(null);
            if(obj != null && obj instanceof ConcurrentMap<?, ?>) {
                storage.setState((ConcurrentMap<String, VersionChain>)obj);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        storage.put(null, 0, 0);
    }
    
}
