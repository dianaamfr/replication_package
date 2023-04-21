package com.dissertation.validation.logs;

import org.json.JSONObject;

public class WriteRequestLog extends Log {
   private final String key;
   private final int partition;

   public WriteRequestLog(String key, int partition, long time) {
      super(LogType.WRITE_REQUEST, time);
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
