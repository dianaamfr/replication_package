package com.dissertation.utils.record;

import org.json.JSONObject;

public class WriteRecord extends Record {
   private final String version;
   private final String key;
   private final int partition;

   public WriteRecord(NodeType nodeType, String nodeId, String version, String key, int partition, boolean isRequest) {
      super(nodeType, nodeId, isRequest ? LogType.WRITE_RECEIVE : LogType.WRITE_RESPOND);
      this.key = key;
      this.partition = partition;
      this.version = version;
   }

   public WriteRecord(NodeType nodeType, String nodeId, String version, String key, int partition, boolean isRequest, long time) {
      super(nodeType, nodeId, isRequest ? LogType.WRITE_RECEIVE : LogType.WRITE_RESPOND, time);
      this.key = key;
      this.partition = partition;
      this.version = version;
   }

   @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("version", this.version)
               .put("key", this.key)
               .put("partition", this.partition);
   }
}
