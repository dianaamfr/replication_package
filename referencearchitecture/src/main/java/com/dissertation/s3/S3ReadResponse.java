package com.dissertation.s3;

public class S3ReadResponse {
    private Long timestamp;
    private String content;

    public S3ReadResponse(String key) {
        this.timestamp = Long.valueOf(key.split("Logs/")[1]);
    }

    public S3ReadResponse() {}

    public S3ReadResponse(String key, String content) {
        this.timestamp = Long.valueOf(key.split("Logs/")[1]);
        this.content = content;
    }

    public boolean hasContent() {
        return this.content != null;
    }

    public String getContent() {
        return this.content;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }
}
