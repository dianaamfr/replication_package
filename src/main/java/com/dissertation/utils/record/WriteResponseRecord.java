package com.dissertation.utils.record;

import org.json.JSONObject;

public class WriteResponseRecord extends Record {
   private final String key;
   private final String version;
   private final int partition;

   public WriteResponseRecord(NodeType nodeType, String nodeId, String key, int partition, String version) {
      super(nodeType, nodeId, LogType.WRITE_RESPONSE);
      this.key = key;
      this.partition = partition;
      this.version = version;
   }

   @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("key", this.key)
               .put("partition", this.partition)
               .put("version", this.version);
   }
}
