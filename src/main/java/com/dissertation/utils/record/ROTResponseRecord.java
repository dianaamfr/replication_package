package com.dissertation.utils.record;

import org.json.JSONObject;

public class ROTResponseRecord extends Record {
   private final long id;
   private final String stableTime;

   public ROTResponseRecord(NodeType nodeType, String nodeId, long id, String stableTime) {
      super(nodeType, nodeId, LogType.ROT_RESPOND);
      this.id = id;
      this.stableTime = stableTime;
   }

   @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("id", id)
            .put("stableTime", this.stableTime);
   }
}
