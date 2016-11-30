package com.main.json;

public class InvalidJsonException extends RuntimeException {

    private final String json;

    public InvalidJsonException() {
        json = null;
    }

    public InvalidJsonException(String message) {
        super(message);
        json = null;
    }

    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
        json = null;
    }

    public InvalidJsonException(Throwable cause) {
        super(cause);
        json = null;
    }

    public InvalidJsonException(final Throwable cause, final String json) {
        super(cause);
        this.json = json;
    }

    public String json() {
        return json;
    }
}
