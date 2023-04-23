package com.dissertation.validation.logs;

import org.json.JSONObject;

import com.dissertation.utils.Utils;

public class GoodputLog extends Log {
    private long totalBytes;

    public GoodputLog(long totalVersions, long time) {
        super(LogType.GOODPUT, time);
        this.totalBytes = totalVersions * Utils.PAYLOAD_SIZE_LONG;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("totalBytes", this.totalBytes);
    }
}
