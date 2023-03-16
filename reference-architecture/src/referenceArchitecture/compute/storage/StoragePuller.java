package referenceArchitecture.compute.storage;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONObject;

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
            String jsonString = this.dataStoreStub.read(null);
            System.out.println(jsonString);
            // if(json != null) {
            //     storage.setState(parseJson(json));
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ConcurrentMap<String, VersionChain> parseJson(JSONObject json) {
        JSONObject response = json.getJSONObject("response");
        // response.getString("timestamp");
        response.getJSONObject("versionChains");
        return null;
    }
    
}
