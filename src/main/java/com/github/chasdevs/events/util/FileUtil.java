package com.github.chasdevs.events.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    public static List<Path> getChildren(Path path) throws IOException {
        if(path != null && path.toFile().isDirectory()) {
            return Files.list(path).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("The path specified must be that of a directory.");
        }
    }

    public static List<Path> getDescendents(Path path, List<Path> exclusions) throws IOException {
        List<Path> descendents = new ArrayList<>();
        for(Path child : getChildren(path)) {
            if(child.toFile().isDirectory()) {
                if(!exclusions.contains(child)) {
                    descendents.addAll(getDescendents(child, exclusions));
                }
            } else {
                descendents.add(child);
            }
        }
        return descendents;
    }


}
