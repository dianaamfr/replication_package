package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class WriteRequestLog extends Log {
   private final int partition;
   private final String version;

   public WriteRequestLog(int partition, String version, long time) {
      super(LogType.WRITE_REQUEST, time);
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
