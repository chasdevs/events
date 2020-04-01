package com.github.chasdevs.events.command;

/**
 * Generic runtime exception to throw back to the interactive shell or command line runner when appropriate.
 */
public class SchemaCommandException extends RuntimeException {

    public SchemaCommandException(String message) {
        super(message);
    }

    public SchemaCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
