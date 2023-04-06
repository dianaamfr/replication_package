package com.dissertation.referencearchitecture.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public final class Config {
    private static final Set<String> partitions;
    private static final Map<Region, Set<String>> partitionsPerRegion;
    private static final Map<String, Set<Region>> regionsWithPartition;

    static {
        partitions = new HashSet<>();
        partitionsPerRegion = new HashMap<>();
        regionsWithPartition = new HashMap<>();
        init();
    }

    private static void init() {
        String partition1 = String.format(Utils.S3_PARTITION_FORMAT, 1);
        String partition2 = String.format(Utils.S3_PARTITION_FORMAT, 2);
        partitions.add(partition1);
        partitions.add(partition2);

        partitionsPerRegion.put(Utils.DEFAULT_REGION, new HashSet<>(Arrays.asList(partition1, partition2)));
  
        regionsWithPartition.put(partition1, new HashSet<>(Arrays.asList(Utils.DEFAULT_REGION)));
        regionsWithPartition.put(partition2, new HashSet<>(Arrays.asList(Utils.DEFAULT_REGION)));
    }

    public static boolean isRegion(Region region) { 
        return partitionsPerRegion.containsKey(region);
    }

    public static boolean isPartition(String partition) {
        return partitions.contains(partition);
    }

    public static String getKeyPartition(Region region, String key) throws KeyNotFoundException {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(Utils.S3_PARTITION_FORMAT, partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            throw new KeyNotFoundException();
        }

        return partitionName;
    }

    public static boolean isKeyInRegion(Region region, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(Utils.S3_PARTITION_FORMAT, partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            return false;
        }

        return true;
    }

    public static boolean isKeyInPartition(String partition, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(Utils.S3_PARTITION_FORMAT, partitionId);

        return partitionName.equals(partition);
    }

    public static Set<Region> getRegions() {
        return partitionsPerRegion.keySet();
    }

    public static Set<String> getPartitions() {
        return partitions;
    }   
    
    public static Set<String> getPartitions(Region region) {
        return partitionsPerRegion.get(region);
    }   

    public static Set<Region> getPartitionRegions(String partition) {
        return regionsWithPartition.get(partition);
    }   
}
