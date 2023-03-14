package referenceArchitecture.compute.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class Storage {
    ConcurrentMap<String, VersionChain> keyVersions = new ConcurrentHashMap<>();

    public void put(String key, String timestamp, Integer value) {
        if(!keyVersions.containsKey(key)){
            keyVersions.put(key, new VersionChain());
        }
        keyVersions.get(key).put(timestamp, value);
    }

    public Integer get(String key, String timestamp) throws KeyNotFoundException, KeyVersionNotFoundException {
        if(!keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return keyVersions.get(key).get(timestamp);
    }
}
