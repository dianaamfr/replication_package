package com.dissertation.referencearchitecture.remoteInterface.response;

import com.dissertation.utils.Response;
import com.dissertation.utils.Utils;

public class WriteResponse extends Response {
    private final String timestamp;

    public WriteResponse() {
        super();
        this.timestamp = Utils.MIN_TIMESTAMP;
    }

    public WriteResponse(String timestamp) {
        super();
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return this.timestamp;
    }
}
