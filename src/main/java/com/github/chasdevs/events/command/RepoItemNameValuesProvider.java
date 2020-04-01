package com.github.chasdevs.events.command;

import com.github.chasdevs.events.services.repo.RepoService;
import com.github.chasdevs.events.services.repo.RepoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
class RepoItemNameValuesProvider extends ValueProviderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepoItemNameValuesProvider.class);

    private final RepoService repoService;

    @Autowired
    public RepoItemNameValuesProvider(RepoService repoService) {
        this.repoService = repoService;
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        List<CompletionProposal> completionProposals = new ArrayList<>();
        try {
            completionProposals = repoService.listAll().stream().map(item -> new CompletionProposal(item.getName())).collect(Collectors.toList());
        } catch (RepoServiceException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return completionProposals;
    }
}