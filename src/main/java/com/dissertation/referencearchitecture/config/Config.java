package com.dissertation.referencearchitecture.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;

public final class Config {
    private static Set<String> partitions;
    private static Map<String, Set<String>> partitionsPerRegion;

    static {
        partitions = new HashSet<>();
        partitionsPerRegion = new HashMap<>();
        init();
    }

    private static void init() {
        partitions.add("partition1");
        partitions.add("partition2");
        partitions.add("partition3");

        partitionsPerRegion.put("us-east-1", new HashSet<>(Arrays.asList("partition1","partition2")));
        partitionsPerRegion.put("us-west-1", new HashSet<>(Arrays.asList("partition3")));
    }

    public static boolean isRegion(String region) { 
        return partitionsPerRegion.containsKey(region);
    }

    public static boolean isPartition(String partition) {
        return partitions.contains(partition);
    }

    public static String getKeyPartition(String region, String key) throws KeyNotFoundException {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format("partition%d", partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            throw new KeyNotFoundException();
        }

        return partitionName;
    }

    public static boolean isKeyInRegion(String region, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format("partition%d", partitionId);

        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partitionName)) {
            return false;
        }

        return true;
    }

    public static boolean isKeyInPartition(String partition, String key) {
        int partitionId = Math.floorMod(key.hashCode(), partitions.size()) + 1;
        String partitionName = String.format("partition%d", partitionId);

        return partitionName.equals(partition);
    }
    
    public static Set<String> getPartitions(String region) {
        return partitionsPerRegion.get(region);
    }   
}
