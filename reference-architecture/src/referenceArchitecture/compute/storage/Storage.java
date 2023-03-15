package referenceArchitecture.compute.storage;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;

public class Storage {
    private ConcurrentMap<String, VersionChain> keyVersions;
    private long stableTime;

    public Storage() {
        this.keyVersions = new ConcurrentHashMap<>();
        this.stableTime = 0;
    }

    public void put(String key, long timestamp, int value) {
        if(!this.keyVersions.containsKey(key)){
            VersionChain versionChain = new VersionChain(new TreeMap<>());
            this.keyVersions.put(key, versionChain);
        }
        this.keyVersions.get(key).put(timestamp, value);
    }

    public Entry<Long, Integer> get(String key, long maxTimestamp) throws KeyNotFoundException, KeyVersionNotFoundException {
        if(!this.keyVersions.containsKey(key)) {
            throw new KeyNotFoundException();
        }
        return this.keyVersions.get(key).get(maxTimestamp);
    }

    public void setStableTime() {
        long minTime = stableTime;
        Iterator<String> itr = this.keyVersions.keySet().iterator();

        if(itr.hasNext()) {
            String key = itr.next();
            long ts = this.keyVersions.get(key).getMaxTimestamp();
            minTime = ts;
        }
        
        while(itr.hasNext()) {
            String key = itr.next();
            long ts = this.keyVersions.get(key).getMaxTimestamp();
            if(minTime > ts){
                minTime = ts;
            }
        }
        this.stableTime = minTime;
    }

    public long getStableTime() {
        return this.stableTime;
    }
}
