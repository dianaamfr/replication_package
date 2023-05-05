package com.dissertation.eventual.s3;

public class S3ReadResponse extends S3Response {
    private final String content;

    public S3ReadResponse(String content) {
        super();
        this.content = content;
    }

    public S3ReadResponse() {
        this("");
    }

    public String getContent() {
        return this.content;
    }
}
