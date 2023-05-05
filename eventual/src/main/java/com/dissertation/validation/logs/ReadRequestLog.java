package com.dissertation.validation.logs;

import org.json.JSONObject;

public class ReadRequestLog extends Log {
    private final long id;

    public ReadRequestLog(long id, long time) {
        super(LogType.ROT_REQUEST, time);
        this.id = id;
    }

    @Override
    public JSONObject toJson() {
        return super.toJson()
                .put("id", this.id);
    }
}
