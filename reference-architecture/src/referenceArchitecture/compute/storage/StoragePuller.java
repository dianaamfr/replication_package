package referenceArchitecture.compute.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
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
            if(jsonString != null) {
                storage.setState(parseJson(jsonString));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ConcurrentMap<String, VersionChain> parseJson(String json) {
        ConcurrentMap<String, VersionChain> state = new ConcurrentHashMap<>();
        JSONObject response = new JSONObject(json);
    
        // Log Timestamp
        // response.getString("key"); 

        // Log/State
        JSONObject responseJson = new JSONObject(response.getString("value"));
        JSONArray versionChainsJson = responseJson.getJSONArray("state");

        for(int i = 0; i < versionChainsJson.length(); i++) {
            JSONObject versionChainJson = versionChainsJson.getJSONObject(i);
            JSONArray versionChainArray = versionChainJson.getJSONArray("value");
            VersionChain versionChain = new VersionChain();
            for(int j = 0; j < versionChainArray.length(); j++) {
                JSONObject versionJson = versionChainArray.getJSONObject(j);
                versionChain.put(Long.parseLong(versionJson.getString("key")), versionJson.getInt("value"));
            }
            state.put(versionChainJson.getString("key"), versionChain);
        }

        return state;
    }
    
}
