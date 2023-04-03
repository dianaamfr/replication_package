package com.dissertation.referencearchitecture.remoteInterface.response;

public class ROTError extends ROTResponse {
    
    public ROTError(String status) {
        super();
        this.status = status;
    }

    @Override
    public boolean isError() {
        return true;
    }
}
