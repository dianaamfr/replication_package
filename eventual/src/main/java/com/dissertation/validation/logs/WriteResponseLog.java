package com.dissertation.validation.logs;

import org.json.JSONObject;

public class WriteResponseLog extends Log {
   private final long id;
   private final int partition;

   public WriteResponseLog(long id, int partition, long time) {
      super(LogType.WRITE_RESPONSE, time);
      this.id = id;
      this.partition = partition;
   }

   @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("id", this.id)
            .put("partition", this.partition);
   }
}
