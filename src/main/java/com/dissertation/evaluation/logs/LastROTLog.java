package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class LastROTLog extends Log {
    private long lastId;

    public LastROTLog(long lastId) {
        super(LogType.LAST_ROT);
        this.lastId = lastId;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("totalReads", this.lastId);
    }
}
