package com.dissertation.eventual.utils;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class Utils { 
    public static final Region DEFAULT_REGION = Region.US_EAST_1;
    private static final String BUCKET_SUFFIX = System.getProperty("bucketSuffix");
    public static final String S3_PARTITION_FORMAT = "p%d-%s" + BUCKET_SUFFIX;

    public static final String S3_ENDPOINT = System.getenv("S3_ENDPOINT");
    public static final int NUM_PARTITIONS = Integer.parseInt(System.getProperty("partitions"));


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

}
