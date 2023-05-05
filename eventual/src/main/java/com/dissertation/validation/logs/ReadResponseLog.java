package com.dissertation.validation.logs;

import org.json.JSONObject;

public class ReadResponseLog extends Log {
    private final long id;

    public ReadResponseLog(long id, long time) {
        super(LogType.ROT_RESPONSE, time);
        this.id = id;
    }

    @Override
    public JSONObject toJson() {
        return super.toJson()
                .put("id", this.id);
    }
}
