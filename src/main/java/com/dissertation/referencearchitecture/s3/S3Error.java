package com.dissertation.referencearchitecture.s3;

public class S3Error extends S3ReadResponse {
    public S3Error(String status) {
        super();
        this.status = status;
    }

    @Override
    public boolean isError() {
        return true;
    }
}
