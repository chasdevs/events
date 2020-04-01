package com.github.chasdevs.events.command;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * This is a value converter used specifically to get around a limitation of Spring Shell. Spring Shell will only allow
 * default values of {@link org.springframework.shell.standard.ShellOption}s to be constants which prevents defaulting
 * the value of an optional String shell option to null.
 * <p>
 * In order to make it look like null is the default value in the help of the command, a value of "null" is specified
 * as the default value. This converter converts any "null" or empty string values to a legitimate null for handling
 * within the {@link org.springframework.shell.standard.ShellMethod}.
 */
@Component
public class RepoItemNameConverter implements Converter<String, String> {
    @Override
    public String convert(String source) {
        String converted;
        if(source.equalsIgnoreCase("null") || source.isBlank()) {
            converted = null;
        } else {
            converted = source;
        }
        return converted;
    }
}
