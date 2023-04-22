package com.dissertation.validation.logs;

import org.json.JSONObject;

import com.dissertation.utils.Utils;

public class GoodputLog extends Log {
    private long endTime;
    private long totalBytes;

    public GoodputLog(long startTime, long endTime, long totalVersions) {
        super(LogType.GOODPUT, startTime);
        this.endTime = endTime;
        this.totalBytes = totalVersions * Utils.PAYLOAD_SIZE_LONG;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("endTime", this.endTime)
                .put("totalBytes", this.totalBytes);
    }
}
