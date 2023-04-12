package com.dissertation.utils.record;

import org.json.JSONObject;

import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public abstract class Record {
    protected final Region region;
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
        WRITE_RECEIVE,
        ROT_RECEIVE,
        WRITE_RESPOND,
        ROT_RESPOND,
        STABLE_TIME,
        LOG_PULL,
        LOG_PUSH,
        STORE_VERSION
    }

    protected Record(NodeType nodeType, String nodeId, LogType logType, long time) {
        this.region = Utils.getCurrentRegion();
        this.nodeType = nodeType;
        this.nodeId = nodeId;
        this.logType = logType;
        this.time = time;
    }

    protected Record(NodeType nodeType, String nodeId, LogType logType) {
        this(nodeType, nodeId, logType, System.currentTimeMillis());
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("region", this.region.toString())
                .put("nodeType", this.nodeType.toString())
                .put("nodeId", this.nodeId)
                .put("time", this.time)
                .put("logType", this.logType.toString());
    }
}