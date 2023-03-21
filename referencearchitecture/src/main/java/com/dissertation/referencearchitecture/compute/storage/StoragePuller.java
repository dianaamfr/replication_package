package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.datastore.DataStoreInterface;

public class StoragePuller implements Runnable {
    DataStoreInterface dataStoreStub;
    ReaderStorage storage;

    public StoragePuller(ReaderStorage storage, DataStoreInterface dataStoreStub) {
        this.dataStoreStub = dataStoreStub;
        this.storage = storage;
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
    }

    private void pull() {
        for(Entry<Integer, Long> entry: this.storage.getPartitionsMaxTimestamp().entrySet()) {
            try {
                String jsonString = this.dataStoreStub.read(String.valueOf(entry.getValue()), entry.getKey());
                if(jsonString != null) {
                    parseJson(jsonString, entry.getKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(String.format("Error: Error when pulling from partition %d", entry.getKey()));
            }
        }
    }

    private void parseJson(String json, Integer partition) throws Exception {
        JSONObject response = new JSONObject(json);

        if(!response.has("key")) {
            return;
        }

        // Log Timestamp
        long timestamp = Long.valueOf(response.getString("key"));
        this.storage.setPartitionMaxTimestamp(partition, timestamp);
        
        // Log/State
        JSONObject responseJson = new JSONObject(response.getString("value"));
        JSONArray versionChainsJson = responseJson.getJSONArray("state");

        for(int i = 0; i < versionChainsJson.length(); i++) {
            JSONObject versionChainJson = versionChainsJson.getJSONObject(i);
            JSONArray versionChainArray = versionChainJson.getJSONArray("value");
            for(int j = 0; j < versionChainArray.length(); j++) {
                JSONObject versionJson = versionChainArray.getJSONObject(j);
                this.storage.put(versionChainJson.getString("key"), Long.parseLong(versionJson.getString("key")), versionJson.getInt("value"));
            }
        }
    }
    
}
