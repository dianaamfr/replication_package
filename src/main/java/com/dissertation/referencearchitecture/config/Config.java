package com.dissertation.referencearchitecture.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;

import software.amazon.awssdk.regions.Region;

public final class Config {
    private static Set<String> partitions;
    private static Map<Region, Set<String>> partitionsPerRegion;
    private static Map<String, Set<Region>> regionsWithPartition;
    public static final String PARTITION_FORMAT = "reference-architecture-partition%d";

    static {
        partitions = new HashSet<>();
        partitionsPerRegion = new HashMap<>();
        regionsWithPartition = new HashMap<>();
        init();
    }

    private static void init() {
        String partition1 = String.format(PARTITION_FORMAT, 1);
        String partition2 = String.format(PARTITION_FORMAT, 2);
        partitions.add(partition1);
        partitions.add(partition2);

        partitionsPerRegion.put(Region.EU_NORTH_1, new HashSet<>(Arrays.asList(partition1, partition2)));
  
        regionsWithPartition.put(partition1, new HashSet<>(Arrays.asList(Region.EU_NORTH_1)));
        regionsWithPartition.put(partition2, new HashSet<>(Arrays.asList(Region.EU_NORTH_1)));
    }

    public static boolean isRegion(Region region) { 
        return partitionsPerRegion.containsKey(region);
    }

    public static boolean isPartition(String partition) {
        return partitions.contains(partition);
    }

    public static String getKeyPartition(Region region, String key) throws KeyNotFoundException {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(PARTITION_FORMAT, partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            throw new KeyNotFoundException();
        }

        return partitionName;
    }

    public static boolean isKeyInRegion(Region region, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(PARTITION_FORMAT, partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            return false;
        }

        return true;
    }

    public static boolean isKeyInPartition(String partition, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format(PARTITION_FORMAT, partitionId);

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
