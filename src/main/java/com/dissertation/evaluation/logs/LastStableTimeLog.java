package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class LastStableTimeLog extends Log {
    private String lastStableTime;

    public LastStableTimeLog(String lastStableTime, long elapsedTime) {
        super(LogType.LAST_STABLE_TIME, elapsedTime);
        this.lastStableTime = lastStableTime;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("lastStableTime", this.lastStableTime);
    }
}
