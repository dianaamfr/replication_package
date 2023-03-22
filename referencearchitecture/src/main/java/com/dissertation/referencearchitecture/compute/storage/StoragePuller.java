package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.s3.S3Helper;
import com.dissertation.s3.S3ReadResponse;

public class StoragePuller implements Runnable {
    S3Helper s3Helper;
    ReaderStorage storage;

    public StoragePuller(ReaderStorage storage, S3Helper s3Helper) {
        this.s3Helper = s3Helper;
        this.storage = storage;
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
    }

    private void pull() {
        for(Entry<String, Long> entry: this.storage.getPartitionsMaxTimestamp().entrySet()) {
            try {
                S3ReadResponse s3Response = this.s3Helper.getObjectAfter(entry.getKey(), String.valueOf(entry.getValue()));
                if(s3Response.getTimestamp() != null && s3Response.getContent() != null) {
                    this.storage.setPartitionMaxTimestamp(entry.getKey(), s3Response.getTimestamp());
                    parseJson(s3Response.getContent(), entry.getKey());
                    System.out.println(this.storage.getState());    
                }   
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(String.format("Error: Error when pulling from partition %s", entry.getKey()));
            }
        }
    }

    private void parseJson(String json, String partition) throws Exception {
        JSONObject response = new JSONObject(json);
        JSONArray versionChainsJson = response.getJSONArray("state");

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
