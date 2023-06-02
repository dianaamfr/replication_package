package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class ROTCountLog extends Log {
    private long lastId;

    public ROTCountLog(long lastId) {
        super(LogType.ROT_COUNT);
        this.lastId = lastId;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("totalReads", this.lastId);
    }
}
