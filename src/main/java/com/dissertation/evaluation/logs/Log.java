package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public abstract class Log {
    protected final long time;
    protected final LogType logType;

    public enum LogType {
        WRITE_REQUEST,
        ROT_REQUEST,
        WRITE_RESPONSE,
        ROT_RESPONSE,
        STABLE_TIME,
        LOG_PULL,
        LOG_PUSH,
        STORE_VERSION,
        GOODPUT,
        LAST_STABLE_TIME,
        STABLE_TIME_VERSIONS,
        THROUGHPUT
    }

    protected Log(LogType logType, long time) {
        this.logType = logType;
        this.time = time;
    }

    protected Log(LogType logType) {
        this(logType, System.currentTimeMillis());
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("time", this.time)
                .put("logType", this.logType.toString());
    }
}