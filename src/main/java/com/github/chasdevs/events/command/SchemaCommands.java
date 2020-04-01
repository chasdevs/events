package com.github.chasdevs.events.command;

import com.github.chasdevs.events.services.mediation.MediationService;
import com.github.chasdevs.events.services.mediation.MediationServiceException;
import com.github.chasdevs.events.services.repo.*;
import com.github.chasdevs.events.clients.registry.SchemaRegistryClient;
import com.github.chasdevs.events.clients.registry.SchemaRegistryException;
import com.github.chasdevs.events.config.SchemaRegistryConfig;
import com.github.chasdevs.events.util.NamingUtil;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents all of the existing commands available in the interactive shell/CLI.
 */
@ShellComponent
public class SchemaCommands {

    private final RepoService repoService;
    private final SchemaRegistryClient registryClient;
    private final SchemaRegistryConfig registryConfig;
    private final MediationService mediationService;

    @Autowired
    public SchemaCommands(RepoService repoService, SchemaRegistryClient registryClient, SchemaRegistryConfig registryConfig, MediationService mediationService) {
        this.repoService = repoService;
        this.registryClient = registryClient;
        this.registryConfig = registryConfig;
        this.mediationService = mediationService;
    }

    @ShellMethod("Lists all schemas")
    public List<String> list(@ShellOption(valueProvider = RepoItemTypeValueProvider.class, defaultValue = "null") RepoItemType type) {
        //todo - add remote option
        List<RepoItem> items;
        try {
            if(type != null) {
                items = repoService.list(type);
            } else {
                items = repoService.listAll();
            }
        } catch (RepoServiceException e) {
            throw new SchemaCommandException(e.getMessage(), e);
        }
        //todo - sort and add sexier toString
        return items.stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    @ShellMethod("Prints the current state of a schema (fully expanded)")
    public String print(@ShellOption(valueProvider = RepoItemNameValuesProvider.class) String name,
                        @ShellOption boolean remote) {
        String schemaString;
        try {
            if(!remote) {
                schemaString = repoService.getSchema(name).toString(true);
            } else {
                schemaString = registryClient.getLatestSchemaBySubject(NamingUtil.fromLocalToRegistrySubject(name)).toString(true);
            }
        } catch (RepoServiceException | SchemaRegistryException e) {
            throw new SchemaCommandException(e.getMessage(), e);
        }
        return schemaString;
    }

    @ShellMethod("Registers the current local schema with the schema registry")
    public String register(@ShellOption(valueProvider = RepoItemNameValuesProvider.class) String name) {
        //todo - potentially remove or restrict to only being available when environment is local
        String message = "Local schema for %s was successfully registered with the schema registry at %s";
        try {
            Schema schema = repoService.getSchema(name);
            registryClient.register(NamingUtil.fromLocalToRegistrySubject(name), schema);
        } catch (RepoServiceException | SchemaRegistryException e) {
            throw new SchemaCommandException(e.getMessage(), e);
        }
        return String.format(message, name, registryConfig.getUrl());
    }

    @ShellMethod("Synchronizes the local repo with the schema registry by registering new schema versions")
    public String sync(@ShellOption boolean force) {
        if(force) {
            try {
                return mediationService.getPrettyMessageFromSyncResultsMap(mediationService.syncLocalWithRemote());
            } catch (MediationServiceException e) {
                throw new SchemaCommandException(e.getMessage(), e);
            }
        } else {
            return "Use the force, Luke";
        }
    }

    @ShellMethod("Tests the compatibility of the current local schema(s) with the schema registry")
    public String testCompatibility(@ShellOption(valueProvider = RepoItemNameValuesProvider.class, defaultValue = "null") String name) {
        String message;
        try {
            if(name == null) {
                message = mediationService.getPrettyMessageFromCompatibilityResultsMap(mediationService.testGlobalCompatibility());
            } else {
                message = mediationService.testCompatibility(name);
            }
        } catch (MediationServiceException e) {
            throw new SchemaCommandException(e.getMessage(), e);
        }
        return message;
    }

    @ShellMethod("Validates the current schema(s)")
    public String validate(@ShellOption(valueProvider = RepoItemNameValuesProvider.class, defaultValue = "null") String name) {
        try {
            if(name == null) {
                repoService.validateLocalRepo();
            } else {
                repoService.validate(name);
            }
        } catch (RepoServiceException e) {
            throw new SchemaCommandException(e.getMessage(), e);
        }
        return name == null ? "All schemas in the local repo are valid" : String.format("Local schema for %s is valid", name);
    }

}