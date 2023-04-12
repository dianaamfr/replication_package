package com.dissertation.utils.record;

import java.util.Map;

import org.json.JSONObject;

public class ROTRecord extends Record {
    private final Map<String, String> versions;
    private final String stableTime;

    public ROTRecord(NodeType nodeType, String nodeId, Map<String, String> versions, String stableTime, boolean isRequest) {
        super(nodeType, nodeId, isRequest ? LogType.ROT_RECEIVE : LogType.ROT_RESPOND);
        this.versions = versions;
        this.stableTime = stableTime;
     }
  
     public ROTRecord(NodeType nodeType, String nodeId, Map<String, String> versions, String stableTime, boolean isRequest, long time) {
        super(nodeType, nodeId, isRequest ? LogType.ROT_RECEIVE : LogType.ROT_RESPOND, time);
        this.versions = versions;
        this.stableTime = stableTime;
     }
  
     @Override
     public JSONObject toJson() {
        return super.toJson()
                 .put("versions", this.versions)
                 .put("stableTime", this.stableTime);
     }
}
