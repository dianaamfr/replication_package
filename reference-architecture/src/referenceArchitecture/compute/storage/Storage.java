package referenceArchitecture.compute.storage;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class Storage {
    private ConcurrentMap<String, VersionChain> keyVersions = new ConcurrentHashMap<>();
    private String stableTime = "0";

    public void put(String key, String timestamp, Integer value) {
        if(!keyVersions.containsKey(key)){
            VersionChain versionChain = new VersionChain(new TreeMap<>());
            keyVersions.put(key, versionChain);
        }
        keyVersions.get(key).put(timestamp, value);
    }

    public Entry<String, Integer> get(String key, String maxTimestamp) throws KeyNotFoundException, KeyVersionNotFoundException {
        if(!keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return keyVersions.get(key).get(maxTimestamp);
    }

    public void setStableTime() {
        String time = "";
        for(String key: keyVersions.keySet()) {
            String ts = keyVersions.get(key).getMaxTimestamp();
            if(time.equals("") || time.compareTo(ts) > 0){
                time = ts;
            }
        }
        stableTime = time;
    }

    public String getStableTime() {
        return stableTime;
    }
}
