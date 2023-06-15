package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class StableTimeVersionsLog extends Log {
    private final String stableTime;
    private final int versions;

    public StableTimeVersionsLog(String stableTime, int versions) {
        super(LogType.STABLE_TIME_VERSIONS);
        this.stableTime = stableTime;
        this.versions = versions;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
               .put("stableTime", this.stableTime)
               .put("versions", this.versions);
   }
}
