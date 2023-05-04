package com.dissertation.validation.logs;

import org.json.JSONObject;

public class S3OperationLog extends Log {
    private final String version;
    private final int partition;

    public S3OperationLog(String version, int partition, boolean isPush) {
        super(isPush ? LogType.LOG_PUSH : LogType.LOG_PULL);
        this.version = version;
        this.partition = partition;
    }

    @Override
    public JSONObject toJson() {
        return super.toJson()
                .put("version", this.version)
                .put("partition", this.partition);
    }
}
