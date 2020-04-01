package com.github.chasdevs.events.services.mediation;

import com.hotels.avro.compatibility.Compatibility;
import com.hotels.avro.compatibility.CompatibilityCheckResult;
import com.github.chasdevs.events.clients.registry.SchemaRegistryClient;
import com.github.chasdevs.events.clients.registry.SchemaRegistryException;
import com.github.chasdevs.events.services.repo.RepoItemType;
import com.github.chasdevs.events.services.repo.RepoServiceException;
import com.github.chasdevs.events.services.repo.RepoService;
import com.github.chasdevs.events.util.NamingUtil;
import com.github.chasdevs.events.util.Util;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class handles any mediation between the local schema repo and the schema registry. This includes syncing and testing
 * compatibility of local schemas with their remote counterparts.
 */
@Service
public class MediationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediationService.class);

    private SchemaRegistryClient registryClient;
    private RepoService repoService;

    private Map<String, Schema> registrySchemaMap;
    private Map<RepoItemType, Map<String, Schema>> localRepoSchemaMap;

    @Autowired
    public MediationService(SchemaRegistryClient registryClient, RepoService repoService) {
        this.registryClient = registryClient;
        this.repoService = repoService;
    }

    public Map<RepoItemType, Map<String, String>> syncLocalWithRemote() throws MediationServiceException {
        Map<RepoItemType, Map<String, String>> syncResultsMap = new HashMap<>();
        buildLocalAndRemoteMaps();
        for (RepoItemType repoItemType : localRepoSchemaMap.keySet()) {
            Map<String, String> resultsMapForItemType = getSyncResultsMapForItemType(repoItemType);
            if (!resultsMapForItemType.isEmpty()) {
                syncResultsMap.put(repoItemType, resultsMapForItemType);
            }
        }
        if (repoItemMapHasFailures(syncResultsMap)) {
            throw new MediationServiceException(getPrettyMessageFromSyncResultsMap(syncResultsMap));
        } else {
            return syncResultsMap;
        }
    }

    public String getPrettyMessageFromSyncResultsMap(Map<RepoItemType, Map<String, String>> resultsMap) {
        String response;
        if (resultsMap.isEmpty()) {
            response = "The local repo already matches the schema registry. No need to sync.";
        } else {
            if (repoItemMapHasFailures(resultsMap)) {
                StringBuffer sb = new StringBuffer("Sync completed with some failures. Updated schemas and their registration results:\n\n");
                resultsMap.entrySet().stream().forEach(repoItemTypeMapEntry -> sb.append(repoItemTypeMapEntry.getValue().entrySet().stream()
                        .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n", repoItemTypeMapEntry.getKey().getDirectory() + ":\n", "\n\n"))));
                response = sb.toString();
            } else {
                response = "Sync of local repo completed successfully.";
            }
        }
        return response;
    }

    public String testCompatibility(String name) throws MediationServiceException {
        boolean compatible;
        Schema localSchema;
        Schema registrySchema;
        try {
            localSchema = repoService.getSchema(name);
            registrySchema = registryClient.getLatestSchemaBySubject(NamingUtil.fromLocalToRegistrySubject(name));
            if (!localSchema.equals(registrySchema)) {
                if (registrySchema != null) {
                    compatible = registryClient.testCompatibility(NamingUtil.fromLocalToRegistrySubject(name), localSchema);
                } else {
                    return String.format("Schema for %s has not been registered yet. No need to test compatibility.", name);
                }
            } else {
                compatible = true;
            }
        } catch (RepoServiceException | SchemaRegistryException e) {
            LOGGER.debug(e.getMessage());
            throw new MediationServiceException(e.getMessage(), e);
        }

        String message = String.format("Local schema for %s %s compatible with the corresponding schema in the registry", name, (compatible ? "is" : "is NOT"));

        if (!compatible) {
            String betterMessage = this.getAvroIncompatibilityMessage(localSchema, registrySchema);
            throw new MediationServiceException(Util.coalesce(betterMessage, message));
        } else {
            return message;
        }
    }

    public Map<RepoItemType, Map<String, String>> testGlobalCompatibility() throws MediationServiceException {
        Map<RepoItemType, Map<String, String>> compatibilityResultsMap = new HashMap<>();
        buildLocalAndRemoteMaps();
        for (RepoItemType repoItemType : localRepoSchemaMap.keySet()) {
            Map<String, String> resultsMapForItemType = getCompatibilityResultsMapForItemType(repoItemType);
            if (!resultsMapForItemType.isEmpty()) {
                compatibilityResultsMap.put(repoItemType, resultsMapForItemType);
            }
        }
        if (repoItemMapHasFailures(compatibilityResultsMap)) {
            throw new MediationServiceException(getPrettyMessageFromCompatibilityResultsMap(compatibilityResultsMap));
        } else {
            return compatibilityResultsMap;
        }
    }

    public String getPrettyMessageFromCompatibilityResultsMap(Map<RepoItemType, Map<String, String>> resultsMap) {
        String response;
        if (resultsMap.isEmpty()) {
            response = "The local repo already matches the schema registry. No need to check compatibility.";
        } else {
            if (repoItemMapHasFailures(resultsMap)) {
                StringBuffer sb = new StringBuffer("Compatibility check completed with some failures. Updated schemas and their compatibility test results:\n\n");
                resultsMap.forEach((key, value) -> sb.append(
                        value.entrySet().stream()
                                .map(e -> String.format("    %s:\n        %s", e.getKey(), e.getValue()))
                                .collect(Collectors.joining("\n\n"))
                        )
                );
                response = sb.toString();
            } else {
                response = "Compatibility check completed successfully. All local schemas are compatible with their registry counterparts.";
            }
        }
        return response;
    }

    private Map<String, String> getSyncResultsMapForItemType(RepoItemType repoItemType) {
        Map<String, String> resultsMapForItemType = new HashMap<>();
        Map<String, Schema> itemSchemas = localRepoSchemaMap.get(repoItemType);
        for (String name : itemSchemas.keySet()) {
            Schema localSchema = itemSchemas.get(name);
            Schema registrySchema = registrySchemaMap.get(name);
            if (!localSchema.equals(registrySchema)) {
                try {
                    registryClient.register(name, localSchema);
                } catch (SchemaRegistryException e) {
                    LOGGER.debug(e.getMessage());
                    resultsMapForItemType.put(name, e.getMessage());
                }
            }
        }
        return resultsMapForItemType;
    }

    //TODO: Remove entire concept of RepoItemType and cleanup code.
    private Map<String, String> getCompatibilityResultsMapForItemType(RepoItemType repoItemType) {
        Map<String, String> resultsMapForItemType = new HashMap<>();
        Map<String, Schema> itemSchemas = localRepoSchemaMap.get(repoItemType);
        for (String name : itemSchemas.keySet()) {
            Schema localSchema = itemSchemas.get(name);
            Schema registrySchema = registrySchemaMap.get(name);
            if (!localSchema.equals(registrySchema)) {
                try {
                    if (registrySchema != null && !registryClient.testCompatibility(name, localSchema)) {
                        String msg = Util.coalesce(getAvroIncompatibilityMessage(localSchema, registrySchema), "Remote registry says invalid.");
                        resultsMapForItemType.put(name, msg);
                    }
                } catch (SchemaRegistryException e) {
                    LOGGER.error("Error communicating with schema registry: " + e.getMessage());
                }
            }
        }
        return resultsMapForItemType;
    }

    private void buildLocalAndRemoteMaps() throws MediationServiceException {
        try {
            localRepoSchemaMap = repoService.getLocalRepoSchemaMap();
            registrySchemaMap = registryClient.getCurrentRegistrySchemaMap();
        } catch (RepoServiceException | SchemaRegistryException e) {
            LOGGER.debug(e.getMessage());
            throw new MediationServiceException("There was a problem building local and/or remote schema map(s)", e);
        }
    }

    private boolean repoItemMapHasFailures(Map<RepoItemType, Map<String, String>> repoItemMap) {
        for (RepoItemType repoItemType : repoItemMap.keySet()) {
            if (!repoItemMap.get(repoItemType).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private String getAvroIncompatibilityMessage(Schema localSchema, Schema remoteSchema) {
        CompatibilityCheckResult forwards = Compatibility.checkThat(localSchema).canRead(remoteSchema);
        CompatibilityCheckResult backwards = Compatibility.checkThat(localSchema).canBeReadBy(remoteSchema);

        StringBuilder sb = new StringBuilder();
        if (!backwards.isCompatible()) {
            sb.append("Not backwards-compatible: ").append(parseIncompatibilityMessage((backwards)));
        }
        if (!forwards.isCompatible()) {
            if (sb.length() > 0) sb.append("\n");    
            sb.append("Not forwards-compatible: ").append(parseIncompatibilityMessage(forwards));
        }
        
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String parseIncompatibilityMessage(CompatibilityCheckResult check) {
        return check.getResult()
                .getIncompatibilities()
                .stream()
                .map(incompatibility -> String.format("[%s: %s (%s)]", incompatibility.getType(), incompatibility.getMessage(), incompatibility.getLocation()))
                .collect(Collectors.joining(", "));
    }

}
