package com.dissertation.validation.logs;

import org.json.JSONObject;

public class WriteResponseLog extends Log {
   private final String version;
   private final int partition;

   public WriteResponseLog(int partition, String version, long time) {
      super(LogType.WRITE_RESPONSE, time);
      this.partition = partition;
      this.version = version;
   }

   @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("partition", this.partition)
            .put("version", this.version);
   }
}
