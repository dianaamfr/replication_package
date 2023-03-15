package referenceArchitecture.compute.storage;

import java.util.TreeMap;
import java.util.Map.Entry;

import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class VersionChain {
    private TreeMap<Long, Integer> versions;
    private long maxTimestamp;

    public VersionChain(TreeMap<Long, Integer> versions) {
        this.versions = versions;
        maxTimestamp = 0;
    }

    public void put(long timestamp, int value) {
        // Assumes we receive operations in order (we will need to update this when we have the full log)
        if(maxTimestamp < timestamp) {
            maxTimestamp = timestamp;
        }
        versions.put(timestamp, value);
    }

    public Entry<Long, Integer> get(long maxTimestamp) throws KeyVersionNotFoundException {
        try {
            Entry<Long, Integer> entry = versions.floorEntry(maxTimestamp);
            if(entry == null) {
                throw new KeyVersionNotFoundException();
            }
            return entry;
        } catch(Exception e) {
            throw new KeyVersionNotFoundException();
        }
    }

    public long getMaxTimestamp() {
        return maxTimestamp;
    }
}
