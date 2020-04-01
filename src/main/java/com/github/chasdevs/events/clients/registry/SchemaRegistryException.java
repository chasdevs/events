package com.github.chasdevs.events.clients.registry;

/**
 * Generic checked exception for gracefully handling exceptions from other
 * registry client libraries.
 */
public class SchemaRegistryException extends Exception {

    public SchemaRegistryException(String message) {
        super(message);
    }

    public SchemaRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
