package referenceArchitecture.compute.storage;

import java.util.TreeMap;
import java.util.Map.Entry;

import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class VersionChain {
    private TreeMap<String, Integer> versions;
    private String maxTimestamp;

    public VersionChain(TreeMap<String, Integer> versions) {
        this.versions = versions;
        maxTimestamp = "0";
    }

    public void put(String timestamp, Integer value) {
        if(maxTimestamp.compareTo(timestamp) < 0) {
            maxTimestamp = timestamp;
        }
        versions.put(timestamp, value);
    }

    public Entry<String, Integer> get(String maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<String, Integer> entry = versions.floorEntry(maxTimestamp);
            if(entry == null) {
                throw new KeyVersionNotFoundException();
            }
            return entry;
        } catch(Exception e) {
            throw new KeyVersionNotFoundException();
        }
    }

    public String getMaxTimestamp() {
        return maxTimestamp;
    }
}
