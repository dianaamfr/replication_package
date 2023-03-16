package referenceArchitecture.compute.storage;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
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
            System.out.println(json.toString());
            this.dataStoreStub.write(this.logicalClock.toString(), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject toJson(ConcurrentMap<String, VersionChain> state) {
        JSONObject stateJson = new JSONObject();
        JSONArray versionChainsJson = new JSONArray();

        // For each key
        for(Entry<String, VersionChain> versionChain: state.entrySet()) {
            JSONObject versionChainJson = new JSONObject(); // Version chain of the key
            JSONArray versionsJson = new JSONArray(); // Array of versions of a key
            // For each version in the key
            for(Entry<Long, Integer> version: versionChain.getValue().getVersionChain().entrySet()) {
                JSONObject versionJson = new JSONObject();
                versionJson.put("key", version.getKey().toString());
                versionJson.put("value", version.getValue());
                versionsJson.put(versionJson);
            }
            versionChainJson.put("key", versionChain.getKey());
            versionChainJson.put("value", versionsJson);
            versionChainsJson.put(versionChainJson);
        }

        stateJson.put("state", versionChainsJson);
        return stateJson;
    }
}
