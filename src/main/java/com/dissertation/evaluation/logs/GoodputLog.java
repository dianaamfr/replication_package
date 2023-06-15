package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class GoodputLog extends Log {
    private long totalVersions;

    public GoodputLog(long totalVersions, long elapsedTime) {
        super(LogType.GOODPUT, elapsedTime);
        this.totalVersions = totalVersions;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("totalVersions", this.totalVersions);
    }
}
