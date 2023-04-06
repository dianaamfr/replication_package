package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;
import com.dissertation.utils.Utils;

public class StoragePuller implements Runnable {
    private S3Helper s3Helper;
    private ReaderStorage storage;

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
        for(Entry<String, String> entry: this.storage.getPartitionsMaxTimestamp().entrySet()) {
            try {
                S3ReadResponse s3Response = this.s3Helper.getLogAfter(entry.getKey(), String.valueOf(entry.getValue()));
                if(s3Response.hasContent() && s3Response.hasTimestamp()) {
                    this.storage.setPartitionMaxTimestamp(entry.getKey(), s3Response.getTimestamp());
                    parseJson(s3Response.getContent(), entry.getKey());
                    //System.out.println(s3Response.getContent());    
                } else if(s3Response.isError()) {
                    System.err.println(s3Response.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(String.format("Error: Error when pulling from partition %s", entry.getKey()));
            }
        }
    }

    private void parseJson(String json, String partition) throws Exception {
        JSONObject response = new JSONObject(json);
        JSONArray versionChainsJson = response.getJSONArray(Utils.LOG_STATE);

        for(int i = 0; i < versionChainsJson.length(); i++) {
            JSONObject versionChainJson = versionChainsJson.getJSONObject(i);
            JSONArray versionChainArray = versionChainJson.getJSONArray(Utils.LOG_VERSIONS);
            String key = versionChainJson.getString(Utils.LOG_KEY);
            int lastIdx = this.storage.getLastParsedIndex(key);
            for(int j = lastIdx; j < versionChainArray.length(); j++) {
                JSONObject versionJson = versionChainArray.getJSONObject(j);
                this.storage.put(
                    key, 
                    versionJson.getString(Utils.LOG_TIMESTAMP), 
                    Utils.byteArrayFromString(versionJson.getString(Utils.LOG_VALUE)));
            }
            this.storage.setLastParsedIndex(key, versionChainArray.length());
        }
    }
    
}
