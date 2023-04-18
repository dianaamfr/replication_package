package com.dissertation.referencearchitecture.compute.storage;

import java.util.ArrayDeque;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.json.JSONArray;

import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.S3OperationLog;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.StableTimeLog;

public class StoragePuller implements Runnable {
    private S3Helper s3Helper;
    private ReaderStorage storage;
    private ArrayDeque<Log> s3Logs;
    private final String region;

    public StoragePuller(ReaderStorage storage, S3Helper s3Helper, ArrayDeque<Log> s3Logs, String region) {
        this.s3Helper = s3Helper;
        this.storage = storage;
        this.s3Logs = s3Logs;
        this.region = region;
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
        if(Utils.VALIDATION_LOGS) {
            this.s3Logs.add(new StableTimeLog(this.storage.getStableTime()));
        }
    }

    private void pull() {
        for (Entry<Integer, String> entry : this.storage.getPartitionsMaxTimestamp().entrySet()) {
            try {
                String partitionBucket = Utils.getPartitionBucket(entry.getKey(), this.region);
                S3ReadResponse s3Response = this.s3Helper.getLogAfter(partitionBucket,
                        String.valueOf(entry.getValue()));
                if (s3Response.hasContent() && s3Response.hasTimestamp()) {
                    if(Utils.VALIDATION_LOGS) {
                        this.s3Logs.add(new S3OperationLog(s3Response.getTimestamp(), entry.getKey(), false));
                    }
                    this.storage.setPartitionMaxTimestamp(entry.getKey(), s3Response.getTimestamp());
                    parseJson(s3Response.getContent(), entry.getKey());
                    // System.out.println(s3Response.getContent());
                } else if (s3Response.isError()) {
                    System.err.println(s3Response.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(String.format("Error when pulling from partition %d", entry.getKey()));
            }
        }
    }

    private void parseJson(String json, int partition) {
        JSONObject response = new JSONObject(json);
        JSONArray versionChainsJson = response.getJSONArray(Utils.LOG_STATE);

        for (int i = 0; i < versionChainsJson.length(); i++) {
            JSONObject versionChainJson = versionChainsJson.getJSONObject(i);
            JSONArray versionChainArray = versionChainJson.getJSONArray(Utils.LOG_VERSIONS);
            String key = versionChainJson.getString(Utils.LOG_KEY);
            int lastIdx = this.storage.getLastParsedIndex(key);
            for (int j = lastIdx; j < versionChainArray.length(); j++) {
                JSONObject versionJson = versionChainArray.getJSONObject(j);
                String timestamp = versionJson.getString(Utils.LOG_TIMESTAMP);
                this.storage.put(
                        key,
                        timestamp,
                        Utils.byteStringFromString(versionJson.getString(Utils.LOG_VALUE)));
            }
            this.storage.setLastParsedIndex(key, versionChainArray.length());
        }
    }

}
