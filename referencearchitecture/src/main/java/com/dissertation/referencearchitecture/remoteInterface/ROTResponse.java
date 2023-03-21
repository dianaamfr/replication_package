package com.dissertation.referencearchitecture.remoteInterface;

import java.io.Serializable;
import java.util.Map;

public class ROTResponse implements Serializable {
    private static final long serialVersionUID = 2L;
    private Map<String, Integer> values;
    private long stableTime;

    public ROTResponse(Map<String, Integer> values, long stableTime) {
        this.values = values;
        this.stableTime = stableTime;
    }

    public Map<String, Integer> getValues(){
        return this.values;
    }

    public long getStableTime() {
        return this.stableTime;
    }
}
