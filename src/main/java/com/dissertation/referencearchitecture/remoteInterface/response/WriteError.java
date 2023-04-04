package com.dissertation.referencearchitecture.remoteInterface.response;

public class WriteError extends WriteResponse {
    public WriteError(String status) {
        super();
        this.status = status;
    }

    @Override
    public boolean isError() {
        return true;
    }
}
