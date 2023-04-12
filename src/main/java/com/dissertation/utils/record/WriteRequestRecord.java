package com.dissertation.utils.record;

import org.json.JSONObject;

public class WriteRequestRecord extends Record {
   private final String key;
   private final int partition;

   public WriteRequestRecord(NodeType nodeType, String nodeId, String key, int partition, long time) {
      super(nodeType, nodeId, LogType.WRITE_RECEIVE, time);
      this.key = key;
      this.partition = partition;
   }
   
   @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("key", this.key)
               .put("partition", this.partition);
   }
}
