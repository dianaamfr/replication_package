package com.dissertation.eventual.s3;

public class S3Response {
    protected boolean error;
    protected String status;

    public S3Response() {
        this.error = false;
        this.status = "";
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isError() {
        return this.error;
    }

    public void setError(String status) {
        this.error = true;
        this.status = status;
    }
}
