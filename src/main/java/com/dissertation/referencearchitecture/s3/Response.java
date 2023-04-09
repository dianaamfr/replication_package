package com.dissertation.referencearchitecture.s3;

import java.io.Serializable;

public abstract class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String status;

    public Response() {
        this.status = "";
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isError() {
        return false;
    }
}
