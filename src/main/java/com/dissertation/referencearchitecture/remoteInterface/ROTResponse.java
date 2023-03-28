package com.dissertation.referencearchitecture.remoteInterface;

import java.io.Serializable;
import java.util.Map;

public class ROTResponse implements Serializable {
    private static final long serialVersionUID = 2L;
    private Map<String, Integer> values;
    private String stableTime;

    public ROTResponse(Map<String, Integer> values, String stableTime) {
        this.values = values;
        this.stableTime = stableTime;
    }

    public Map<String, Integer> getValues(){
        return this.values;
    }

    public String getStableTime() {
        return this.stableTime;
    }
}
