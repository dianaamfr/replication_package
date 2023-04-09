package com.dissertation.referencearchitecture.exceptions;

public class KeyVersionNotFoundException extends Exception {
    public KeyVersionNotFoundException() {
        super();
    }

    public KeyVersionNotFoundException(String message, Throwable error) {
        super(message, error);
    }

    @Override
    public String toString() {
        return "No key version found.";
    }
}
