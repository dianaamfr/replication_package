package com.dissertation.referencearchitecture.s3;

public class S3ReadResponse {
    private String timestamp;
    private String content;

    public S3ReadResponse(String key) {
        this.timestamp = key.split("Logs/")[1];
    }

    public S3ReadResponse() {}

    public S3ReadResponse(String key, String content) {
        this.timestamp = key.split("Logs/")[1];
        this.content = content;
    }

    public boolean hasContent() {
        return this.content != null;
    }

    public String getContent() {
        return this.content;
    }

    public String getTimestamp() {
        return this.timestamp;
    }
}
