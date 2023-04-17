package com.dissertation.utils.record;

import org.json.JSONObject;

public class LogOperationRecord extends Record {
    private final String version;
    private final int partition;

    public LogOperationRecord(NodeType nodeType, String nodeId, String version, int partition, boolean isPush) {
        super(nodeType, nodeId, isPush ? LogType.LOG_PUSH : LogType.LOG_PULL);
        this.version = version;
        this.partition = partition;
    }
    
    @Override
     public JSONObject toJson() {
        return super.toJson()
                 .put("version", this.version)
                 .put("partition", this.partition);
     }
}
