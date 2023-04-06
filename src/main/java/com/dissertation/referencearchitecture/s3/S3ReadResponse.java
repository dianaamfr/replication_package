package com.dissertation.referencearchitecture.s3;

public class S3ReadResponse extends Response {
    private String timestamp;
    private String content;

    public S3ReadResponse() {
        super();
        this.timestamp = "";
        this.content = "";
    }

    public S3ReadResponse(String timestamp) {
        this();
        this.timestamp = timestamp;
        this.content = "";
    }

    public S3ReadResponse(String timestamp, String content) {
        this(timestamp);
        this.content = content;
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
}
