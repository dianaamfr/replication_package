package com.dissertation.eventual.s3;

public class S3ReadResponse {
    private final String content;
    protected String status;

    public S3ReadResponse(String content) {
        this.content = content;
        this.status = "";
    }

    public S3ReadResponse() {
        this("");
    }

    public String getContent() {
        return this.content;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isError() {
        return false;
    }
}
