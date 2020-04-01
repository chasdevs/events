package com.github.chasdevs.events.services.mediation;

/**
 * Generic checked exception for gracefully handling exceptions while mediating between the local repo and the registry.
 */
public class MediationServiceException extends Exception {

    public MediationServiceException(String message) {
        super(message);
    }

    public MediationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
