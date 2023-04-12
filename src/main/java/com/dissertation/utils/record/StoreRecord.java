package com.dissertation.utils.record;

import org.json.JSONObject;

public class StoreRecord extends Record {
    private final String version;
    private final String key;
    private final int partition;

    public StoreRecord(NodeType nodeType, String nodeId, String version, String key, int partition) {
        super(nodeType, nodeId, LogType.STORE_VERSION);
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
