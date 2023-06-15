package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class ThroughputLog extends Log {
    private long total;

    public ThroughputLog(long total) {
        super(LogType.THROUGHPUT);
        this.total = total;
    }

    public JSONObject toJson() {
        return super.toJson()
                .put("total", this.total);
    }
}
