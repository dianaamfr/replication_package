package com.dissertation.referencearchitecture.exceptions;

public class KeyNotFoundException extends Exception {
    public KeyNotFoundException() {
        super();
    }

    public KeyNotFoundException(String message, Throwable error) {
        super(message, error);
    }

    @Override
    public String toString() {
        return "Key not found.";
    }
}
