package com.dissertation.validation.logs;

import org.json.JSONObject;

public class WriteResponseLog extends Log {
   private final String key;
   private final String version;
   private final int partition;

   public WriteResponseLog(NodeType nodeType, String nodeId, String key, int partition, String version) {
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
