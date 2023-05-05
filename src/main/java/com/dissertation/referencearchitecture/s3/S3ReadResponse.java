package com.dissertation.referencearchitecture.s3;

public class S3ReadResponse {
    private final String timestamp;
    private final String content;
    protected String status;

    public S3ReadResponse(String timestamp, String content) {
        this.timestamp = timestamp;
        this.content = content;
        this.status = "";
    }

    public S3ReadResponse(String timestamp) {
        this(timestamp, "");
    }

    public S3ReadResponse() {
        this("", "");
    }

    public boolean hasContent() {
        return !this.content.isBlank();
    }

    public boolean hasTimestamp() {
        return !this.timestamp.isBlank();
    }

    public String getContent() {
        return this.content;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isError() {
        return false;
    }
}
