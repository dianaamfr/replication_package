package com.dissertation.validation.logs;

import org.json.JSONObject;

public class ROTResponseLog extends Log {
    private final long id;
    private final String stableTime;

    public ROTResponseLog(long id, String stableTime, long time) {
        super(LogType.ROT_RESPONSE, time);
        this.id = id;
        this.stableTime = stableTime;
    }

    @Override
    public JSONObject toJson() {
        return super.toJson()
                .put("id", this.id)
                .put("stableTime", this.stableTime);
    }
}
