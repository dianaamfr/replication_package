package referenceArchitecture.compute.exceptions;

public class KeyVersionNotFoundException extends Exception {
    public KeyVersionNotFoundException(){
        super();
    }

    public KeyVersionNotFoundException(String message, Throwable error) {
        super(message, error);
    }
}
