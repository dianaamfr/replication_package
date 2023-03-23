package com.dissertation.referencearchitecture.compute.exceptions;

public class KeyNotFoundException extends Exception {
    public KeyNotFoundException(){
        super();
    }

    public KeyNotFoundException(String message, Throwable error) {
        super(message, error);
    }
}
