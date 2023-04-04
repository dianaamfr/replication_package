package com.dissertation.referencearchitecture.exceptions;

public class InvalidRegionException extends Exception {
    public InvalidRegionException(){
        super();
    }

    public InvalidRegionException(String message, Throwable error) {
        super(message, error);
    }

    @Override
    public String toString() {
        return "Invalid region.";
    }
}
