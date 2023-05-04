package com.dissertation.validation.logs;

import org.json.JSONObject;

public class StableTimeLog extends Log {
    private final String stableTime;

    public StableTimeLog(String stableTime) {
        super(LogType.STABLE_TIME);
        this.stableTime = stableTime;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("stableTime", this.stableTime);
   }
}
