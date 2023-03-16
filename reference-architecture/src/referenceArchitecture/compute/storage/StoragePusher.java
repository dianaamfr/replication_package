package referenceArchitecture.compute.storage;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONObject;

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
            JSONObject json = toJson(this.storage.getState());
            System.out.println(this.storage.getState());
            System.out.println(json.toString());
            this.dataStoreStub.write(this.logicalClock.toString(), json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject toJson(ConcurrentMap<String, VersionChain> state) {
        JSONObject json = new JSONObject();
        for(Entry<String, VersionChain> entry: state.entrySet()) {
            JSONObject value = new JSONObject(entry.getValue().getVersionChain());
            json.put(entry.getKey(), value);
        }
        return json;
    }
}
