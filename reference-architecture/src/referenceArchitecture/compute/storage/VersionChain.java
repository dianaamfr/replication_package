package referenceArchitecture.compute.storage;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.Map.Entry;

import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class VersionChain implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeMap<Long, Integer> versions;

    public VersionChain() {
        this.versions = new TreeMap<>();
    }

    public void put(Long timestamp, Integer value) {
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

    @Override
    public String toString() {
        return versions.toString();
    }

    public TreeMap<Long, Integer> getVersionChain() {
        return this.versions;
    }
}
