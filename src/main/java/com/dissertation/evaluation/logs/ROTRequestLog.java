package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class ROTRequestLog extends Log {
    private final long id;

    public ROTRequestLog(long id, long time) {
        super(LogType.ROT_REQUEST, time);
        this.id = id;
    }

    public ROTRequestLog(long id) {
        super(LogType.ROT_REQUEST);
        this.id = id;
    }

    @Override
    public JSONObject toJson() {
        return super.toJson()
                .put("id", this.id);
    }
}
