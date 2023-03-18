package referenceArchitecture.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;

public final class Config {
    private static Map<String, List<Integer>> partitionsPerRegion;
    private static Map<String, Integer> keysPartition;
    private static Map<Integer, List<String>> partitionKeys;

    static {
        partitionsPerRegion = new HashMap<>();
        keysPartition = new HashMap<>();
        partitionKeys = new HashMap<>();

        partitionsPerRegion.put("A", new ArrayList<>(Arrays.asList(1,2)));
        partitionsPerRegion.put("B", new ArrayList<>(Arrays.asList(1)));

        keysPartition.put("x", 1);
        keysPartition.put("y", 1);
        keysPartition.put("z", 2);

        partitionKeys.put(1, new ArrayList<>(Arrays.asList("x", "y")));
        partitionKeys.put(2, new ArrayList<>(Arrays.asList("z")));
    }

    public static boolean isRegion(String region) {
        return partitionsPerRegion.containsKey(region);
    }

    public static Integer getKeyPartition(String region, String key) throws KeyNotFoundException {
        if(!keysPartition.containsKey(key)){
            throw new KeyNotFoundException();
        }
        
        Integer partition = keysPartition.get(key);
        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partition)) {
            throw new KeyNotFoundException();
        }

        return partition;
    }

    public static boolean isKeyInRegion(String region, String key) {
        if(!keysPartition.containsKey(key)){
            return false;
        }
        
        Integer partition = keysPartition.get(key);
        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partition)) {
            return false;
        }

        return true;
    }

    public static boolean isKeyInPartition(Integer partition, String key) {
        if(keysPartition.containsKey(key)) {
            return keysPartition.get(key).equals(partition);
        }
        return false;
    }
    
    public static List<Integer> getPartitions(String region) {
        return partitionsPerRegion.get(region);
    }   

    public static List<String> getKeys(Integer partition) {
        return partitionKeys.get(partition);
    }

    public static boolean isPartitionInRegion(String region, Integer partition) {
        return partitionsPerRegion.get(region).contains(partition);
    }

    public static Set<Integer> getPartitions() {
        return new HashSet<>(keysPartition.values());
    }
}
