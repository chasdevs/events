package com.github.chasdevs.events.command;

import com.github.chasdevs.events.services.repo.RepoItemType;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RepoItemTypeValueProvider extends EnumValueProvider {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return Arrays.stream(RepoItemType.values()).map(value -> new CompletionProposal(value.getLabel())).collect(Collectors.toList());
    }
}
