package com.dissertation.referencearchitecture.remoteInterface.response;

import java.util.HashMap;
import java.util.Map;

import com.dissertation.utils.Response;
import com.dissertation.utils.Utils;

public class ROTResponse extends Response {
    private final Map<String, byte[]> values;
    private final String stableTime;

    public ROTResponse() {
        super();
        this.values = new HashMap<>();
        this.stableTime = Utils.MIN_TIMESTAMP;
    }

    public ROTResponse(Map<String, byte[]> values, String stableTime) {
        super();
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