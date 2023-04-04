package com.dissertation.referencearchitecture.exceptions;

public class InvalidTimestampException extends Exception {
    public InvalidTimestampException(){
        super();
    }

    public InvalidTimestampException(String message, Throwable error) {
        super(message, error);
    }

    @Override
    public String toString() {
        return "Invalid timestamp.";
    }
}
