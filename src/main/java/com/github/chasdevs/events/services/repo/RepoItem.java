package com.github.chasdevs.events.services.repo;

import java.nio.file.Path;
import java.util.Objects;

public class RepoItem {

    private final String name;
    private final RepoItemType repoItemType;
    private final Path pathToFile;

    public RepoItem(String name, RepoItemType repoItemType, Path pathToFile) {
        this.name = name;
        this.repoItemType = repoItemType;
        this.pathToFile = pathToFile;
    }

    public String getName() {
        return name;
    }

    public RepoItemType getRepoItemType() {
        return repoItemType;
    }

    public Path getPathToFile() {
        return pathToFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoItem repoItem = (RepoItem) o;
        return Objects.equals(name, repoItem.name) &&
                repoItemType == repoItem.repoItemType &&
                Objects.equals(pathToFile, repoItem.pathToFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, repoItemType, pathToFile);
    }

    @Override
    public String toString() {
        return name + ": " + pathToFile;
    }
}
