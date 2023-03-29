package com.dissertation.referencearchitecture.remoteInterface;

import java.io.Serializable;
import java.util.Map;

public class ROTResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, byte[]> values;
    private String stableTime;

    public ROTResponse(Map<String, byte[]> values, String stableTime) {
        this.values = values;
        this.stableTime = stableTime;
    }

    public Map<String, byte[]> getValues(){
        return this.values;
    }

    public String getStableTime() {
        return this.stableTime;
    }
}
