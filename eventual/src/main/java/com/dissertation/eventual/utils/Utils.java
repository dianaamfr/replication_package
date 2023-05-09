package com.dissertation.eventual.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;

import org.json.JSONArray;

import com.dissertation.validation.logs.Log;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class Utils { 
    public static final Region DEFAULT_REGION = Region.US_EAST_1;
    private static final String BUCKET_SUFFIX = System.getProperty("bucketSuffix");
    public static final String S3_PARTITION_FORMAT = "p%d-%s" + BUCKET_SUFFIX;

    public static final String S3_ENDPOINT = System.getProperty("s3Endpoint");
    public static final int NUM_PARTITIONS = Integer.parseInt(System.getProperty("partitions"));
    public static final int MAX_LOGS = 300;
    public static final int GOODPUT_TIME = 60000;
    public static final long PAYLOAD_START_LONG = 274877906944L;
    public static final long PAYLOAD_END_LONG = 549755813887L;

    public static final String READ_CLIENT_ID = "readClient";
    public static final String WRITE_CLIENT_ID = "writeClient";

    public static Region getCurrentRegion() {
        DefaultAwsRegionProviderChain regionLookup = new DefaultAwsRegionProviderChain();
        Region region = Utils.DEFAULT_REGION;

        try {
            region = regionLookup.getRegion();
        } catch (SdkClientException e) {
            System.err.println("Warning: Failed to get region. Using default.");
        }
        return region;
    }

    public static int getKeyPartitionId(String key) {
        return Math.floorMod(key.hashCode(), NUM_PARTITIONS) + 1;
    }

    public static String getPartitionBucket(int partitionId, String region) {
        return String.format(Utils.S3_PARTITION_FORMAT, partitionId, region);
    }

    public static void logToFile(ArrayDeque<Log> logs, String file) {
        FileWriter fw;
        try {
            fw = new FileWriter("logs/" + file.toLowerCase() + ".json", false);

            JSONArray jsonLogs = new JSONArray();
            while (!logs.isEmpty()) {
                jsonLogs.put(logs.poll().toJson());
            }
            jsonLogs.write(fw);
            fw.close();

        } catch (IOException e) {
            System.err.println("Failed to write logs to file.");
        }
    }
}
