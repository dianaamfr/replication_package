package referenceArchitecture.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;

public final class Config {
    private static Map<String, List<Integer>> partitionsPerRegion;
    private static Map<String, Integer> keysPartition;

    static {
        partitionsPerRegion = new HashMap<>();
        keysPartition = new HashMap<>();

        partitionsPerRegion.put("A", new ArrayList<>(Arrays.asList(1,2)));
        partitionsPerRegion.put("B", new ArrayList<>(Arrays.asList(1)));

        keysPartition.put("x", 1);
        keysPartition.put("y", 1);
        keysPartition.put("z", 2);
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

    public static boolean isKeyInRegion(String region, String key) throws KeyNotFoundException {
        if(!keysPartition.containsKey(key)){
            return false;
        }
        
        Integer partition = keysPartition.get(key);
        if(!isRegion(region) || !partitionsPerRegion.get(region).contains(partition)) {
            return false;
        }

        return true;
    }

    public static List<Integer> getPartitions(String region) {
        return partitionsPerRegion.get(region);
    }   
}
