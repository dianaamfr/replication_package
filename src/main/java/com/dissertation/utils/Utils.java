package com.dissertation.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;

import com.dissertation.utils.record.Record;
import com.google.protobuf.ByteString;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class Utils {
    public static final String TIMESTAMP_SEPARATOR = "-";
    public static final String TIMESTAMP_FORMAT = "%020d%s%020d";
    public static final String MIN_TIMESTAMP = String.format(TIMESTAMP_FORMAT, 0, TIMESTAMP_SEPARATOR, 0);
    public static final int PULL_DELAY = 10000;
    public static final int PUSH_DELAY = 10000;
    public static final int CLOCK_DELAY = 20000;

    public static final Region DEFAULT_REGION = Region.EU_NORTH_1;
    public static final String S3_LOG_PREFIX = "Logs/";
    public static final String S3_CLOCK_PREFIX = "Clock/";
    public static final String S3_CLOCK_BUCKET = "reference-architecture-clock";
    public static final String S3_PARTITION_FORMAT = "reference-architecture-partition%d";
    public static final int S3_MAX_KEYS = 5;

    public static final String LOG_STATE = "state";
    public static final String LOG_VERSIONS = "versions";
    public static final String LOG_KEY = "key";
    public static final String LOG_VALUE = "value";
    public static final String LOG_TIMESTAMP = "timestamp";

    public static final String S3_ENDPOINT = System.getProperty("s3Endpoint");
    public static final int NUM_PARTITIONS = Integer.parseInt(System.getProperty("partitions"));

    public static final String READ_NODE_ID = "readNode";
    public static final String WRITE_NODE_ID = "writeNode";
    public static final String READ_CLIENT_ID = "readClient";
    public static final String WRITE_CLIENT_ID = "writeClient";

    public static ByteString byteStringFromString(String encodedBuffer) {
        return ByteString.copyFrom(encodedBuffer.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static String stringFromByteString(ByteString byteString) {
        if (byteString.isEmpty()) {
            return null;
        }
        return byteString.toString(StandardCharsets.ISO_8859_1);
    }

    public static ByteString getRandomByteString(int sizeInBytes) {
        byte[] b = new byte[sizeInBytes];
        ThreadLocalRandom.current().nextBytes(b);
        return ByteString.copyFrom(b);
    }

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

    public static String getPartitionBucket(int partitionId) {
        return String.format(Utils.S3_PARTITION_FORMAT, partitionId);
    }

    public static void logToFile(ConcurrentLinkedQueue<Record> logs, String file) {
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
