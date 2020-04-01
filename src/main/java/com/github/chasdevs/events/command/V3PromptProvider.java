package com.github.chasdevs.events.command;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class V3PromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("events:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
    }
}
