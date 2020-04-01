package com.github.chasdevs.events.services.repo;

/**
 * Generic checked exception for gracefully handling exceptions while modifying
 * the local AVDL repository.
 */
public class RepoServiceException extends Exception {

    public RepoServiceException(String message) {
        super(message);
    }

    public RepoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
