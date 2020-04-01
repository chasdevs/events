package com.github.chasdevs.events.services.repo;

/**
 * Enum to represent any schema types we maintain in our local repository. It should help keep this service opinionated
 * about the structure of our local repository, so as to guide anyone in the company creating schemas.
 */
public enum RepoItemType {

    COMMON("common", "common", new String[]{}),
    EVENT("event", "", new String[]{"common"});

    private String label;
    private String directory;
    private String[] excludedSubdirectories;

    RepoItemType(String label, String directory, String[] excludedSubdirectories) {
        this.label = label;
        this.directory = directory;
        this.excludedSubdirectories = excludedSubdirectories;
    }

    public String getLabel() {
        return label;
    }

    public String getLogLabel() {
        return label + " type";
    }

    public String getDirectory() {
        return directory;
    }

    public String[] getExcludedSubdirectories() {
        return excludedSubdirectories;
    }
}
