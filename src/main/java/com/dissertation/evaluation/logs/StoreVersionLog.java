package com.dissertation.evaluation.logs;

import org.json.JSONObject;

public class StoreVersionLog extends Log {
    private final String key;
    private final int partition;
    private final String version;

    public StoreVersionLog(String key, int partition, String version) {
        super(LogType.STORE_VERSION);
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
