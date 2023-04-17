package com.dissertation.validation.logs;

import org.json.JSONObject;

public abstract class Log {
    protected final NodeType nodeType;
    protected final String nodeId;
    protected final long time;
    protected final LogType logType;

public enum NodeType {
    WRITER,
    READER,
    CLIENT
}

public enum LogType {
    WRITE_REQUEST,
    ROT_REQUEST,
    WRITE_RESPONSE,
    ROT_RESPONSE,
    STABLE_TIME,
    LOG_PULL,
    LOG_PUSH,
}

    protected Log(NodeType nodeType, String nodeId, LogType logType, long time) {
        this.nodeType = nodeType;
        this.nodeId = nodeId;
        this.logType = logType;
        this.time = time;
    }

    protected Log(NodeType nodeType, String nodeId, LogType logType) {
        this(nodeType, nodeId, logType, System.currentTimeMillis());
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("nodeType", this.nodeType.toString())
                .put("nodeId", this.nodeId)
                .put("time", this.time)
                .put("logType", this.logType.toString());
    }
}