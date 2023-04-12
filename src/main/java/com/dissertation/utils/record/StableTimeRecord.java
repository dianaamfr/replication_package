package com.dissertation.utils.record;

import org.json.JSONObject;

public class StableTimeRecord extends Record {
    private final String stableTime;

    public StableTimeRecord(NodeType nodeType, String nodeId, String stableTime) {
        super(nodeType, nodeId, LogType.STABLE_TIME);
        this.stableTime = stableTime;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("stableTime", this.stableTime);
   }
}
