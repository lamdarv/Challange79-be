package com.tujuhsembilan.app.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String message) {
        super(message);
    }

    // You can also add additional constructors, for example, to include the cause of the exception
    public ClientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Additional methods or constructors can be added as needed
}

