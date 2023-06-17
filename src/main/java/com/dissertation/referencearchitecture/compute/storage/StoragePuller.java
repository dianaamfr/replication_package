package com.dissertation.referencearchitecture.compute.storage;

import java.util.ArrayDeque;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import org.json.JSONObject;
import org.json.JSONArray;

import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.S3OperationLog;
import com.dissertation.evaluation.logs.StableTimeLog;
import com.dissertation.evaluation.logs.StableTimeVersionsLog;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.referencearchitecture.s3.S3ReadResponse;
import com.dissertation.utils.Utils;

public class StoragePuller implements Runnable {
    private final S3Helper s3Helper;
    private final ReaderStorage storage;
    private final String region;
    private ArrayDeque<Log> s3Logs;

    public StoragePuller(S3Helper s3Helper, ReaderStorage storage, String region, ArrayDeque<Log> s3Logs) {
        this.s3Helper = s3Helper;
        this.storage = storage;
        this.region = region;
        this.s3Logs = s3Logs;
    }

    @Override
    public void run() {
        this.pull();
        this.storage.setStableTime();
        this.storage.pruneState(this.storage.getStableTime());

        if (Utils.VISIBILITY_LOGS) {
            this.s3Logs.add(new StableTimeLog(this.storage.getStableTime()));
        } else if (Utils.GOODPUT_LOGS) {
            this.s3Logs.add(new StableTimeVersionsLog(this.storage.getStableTime(), this.storage.getTotalVersions()));
        }
    }

    private void pull() {
        for (Entry<Integer, String> entry : this.storage.getPartitionsMaxTimestamp().entrySet()) {
            try {
                String partitionBucket = Utils.getPartitionBucket(entry.getKey(), this.region);
                S3ReadResponse s3Response = this.s3Helper.getLogAfter(partitionBucket, entry.getValue());
                if (s3Response.hasContent() && s3Response.hasTimestamp()) {
                    if (Utils.VISIBILITY_LOGS) {
                        this.s3Logs.add(new S3OperationLog(s3Response.getTimestamp(), entry.getKey(), false));
                    }
                    parseJson(s3Response.getContent(), entry.getKey(), entry.getValue());
                    this.storage.setPartitionMaxTimestamp(entry.getKey(), s3Response.getTimestamp());
                } else if (s3Response.isError()) {
                    System.err.println(s3Response.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(String.format("Error when pulling from partition %d", entry.getKey()));
            }
        }
    }

    private void parseJson(String json, int partition, String previousMaxTimestamp) {
        JSONObject response = new JSONObject(json);
        JSONArray versionChainsJson = response.getJSONArray(Utils.LOG_STATE);

        for (int i = 0; i < versionChainsJson.length(); i++) {
            JSONObject versionChainJson = versionChainsJson.getJSONObject(i);
            JSONArray versionChainArray = versionChainJson.getJSONArray(Utils.LOG_VERSIONS);
            String key = versionChainJson.getString(Utils.LOG_KEY);

            StreamSupport.stream(versionChainArray.spliterator(), false)
                    .map(versionJson -> (JSONObject) versionJson)
                    .filter(versionJson -> versionJson.getString(Utils.LOG_TIMESTAMP)
                            .compareTo(previousMaxTimestamp) > 0)
                    .forEach(versionJson -> {
                        String timestamp = versionJson.getString(Utils.LOG_TIMESTAMP);
                        this.storage.put(
                                key,
                                timestamp,
                                Utils.byteStringFromString(versionJson.getString(Utils.LOG_VALUE)));
                    });
        }
    }

}
