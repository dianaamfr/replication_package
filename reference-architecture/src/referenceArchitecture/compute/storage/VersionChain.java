package referenceArchitecture.compute.storage;

import java.util.TreeMap;
import java.util.Map.Entry;

import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class VersionChain {
    TreeMap<String, Integer> versions = new TreeMap<>();

    public void put(String timestamp, Integer value) {
        versions.put(timestamp, value);
    }

    public Integer get(String maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<String, Integer> entry = versions.floorEntry(maxTimestamp);
            if(entry == null) {
                throw new KeyVersionNotFoundException();
            }
            return entry.getValue();
        } catch(Exception e) {
            throw new KeyVersionNotFoundException();
        }
    }
}
