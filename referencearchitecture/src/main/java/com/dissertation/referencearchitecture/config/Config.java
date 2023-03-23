package com.dissertation.referencearchitecture.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dissertation.referencearchitecture.compute.exceptions.KeyNotFoundException;

public final class Config {
    private static Map<String, List<String>> partitionsPerRegion;
    private static Map<String, String> keysPartition;
    private static Map<String, List<String>> partitionKeys;

    static {
        partitionsPerRegion = new HashMap<>();
        keysPartition = new HashMap<>();
        partitionKeys = new HashMap<>();

        partitionsPerRegion.put("us-east-1", new ArrayList<>(Arrays.asList("partition1","partition2")));
        partitionsPerRegion.put("us-west-1", new ArrayList<>(Arrays.asList("partition3")));

        keysPartition.put("x", "partition1");
        keysPartition.put("y", "partition1");
        keysPartition.put("z", "partition2");
        keysPartition.put("p", "partition3");

        partitionKeys.put("partition1", new ArrayList<>(Arrays.asList("x", "y")));
        partitionKeys.put("partition2", new ArrayList<>(Arrays.asList("z")));
        partitionKeys.put("partition3", new ArrayList<>(Arrays.asList("p")));
    }

    public static boolean isRegion(String region) { 
        return partitionsPerRegion.containsKey(region);
    }

    public static boolean isPartition(String partition) {
        return partitionKeys.containsKey(partition);
    }

    public static String getKeyPartition(String region, String key) throws KeyNotFoundException {
        if(!keysPartition.containsKey(key)){
            throw new KeyNotFoundException();
        }
        
        String partition = keysPartition.get(key);
        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partition)) {
            throw new KeyNotFoundException();
        }

        return partition;
    }

    public static boolean isKeyInRegion(String region, String key) {
        if(!keysPartition.containsKey(key)){
            return false;
        }
        
        String partition = keysPartition.get(key);
        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partition)) {
            return false;
        }

        return true;
    }

    public static boolean isKeyInPartition(String partition, String key) {
        if(keysPartition.containsKey(key)) {
            return keysPartition.get(key).equals(partition);
        }
        return false;
    }
    
    public static List<String> getPartitions(String region) {
        return partitionsPerRegion.get(region);
    }   

    public static List<String> getKeys(String partition) {
        return partitionKeys.get(partition);
    }

    public static boolean isPartitionInRegion(String region, String partition) {
        return partitionsPerRegion.get(region).contains(partition);
    }

    public static Set<String> getPartitions() {
        return new HashSet<>(keysPartition.values());
    }
}
