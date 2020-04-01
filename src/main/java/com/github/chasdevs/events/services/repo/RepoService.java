package com.github.chasdevs.events.services.repo;

import com.github.chasdevs.events.config.LocalRepoConfig;
import com.github.chasdevs.events.util.AvroParser;
import com.github.chasdevs.events.util.Constants;
import com.github.chasdevs.events.util.FileUtil;
import com.github.chasdevs.events.util.NamingUtil;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class handles all I/O with the local file system/schema repo for creating, updating, and reading schemas.
 */
@Service
public class RepoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepoService.class);

    private final Path rootRepoPath;
    private final int allowedNamespaceDepth;

    @Autowired
    public RepoService(LocalRepoConfig localRepoConfig) {
        rootRepoPath = Paths.get(localRepoConfig.getRootPath());
        if(Files.notExists(rootRepoPath)) {
            throw new IllegalArgumentException("The provided local repo path " + rootRepoPath + " does not exist.");
        }
        allowedNamespaceDepth = localRepoConfig.getAllowedNamespaceDepth();
    }

    public RepoItem getItem(String name) throws RepoServiceException {
        return listAll().stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new RepoServiceException(String.format("Could not find any schemas with the name %s", name)));
    }

    public List<RepoItem> list(RepoItemType repoItemType) throws RepoServiceException {
        return getItems(repoItemType);
    }

    public List<RepoItem> listAll() throws RepoServiceException {
        List<RepoItem> allItems = new ArrayList<>();
        for(RepoItemType itemType : RepoItemType.values()) {
            allItems.addAll(list(itemType));
        }
        return allItems;
    }

    public Schema getSchema(String name) throws RepoServiceException {
        RepoItem repoItem = getItem(name);
        Schema schema = getSchema(repoItem);
        if(schema == null) {
            throw new RepoServiceException(String.format("Could not retrieve schema for %s", name));
        } else {
            return schema;
        }
    }

    public void validateLocalRepo() throws RepoServiceException{
        Map<RepoItemType, Map<String, String>> itemsWithValidationErrorsByType = getMapOfValidationErrors();
        if(!itemsWithValidationErrorsByType.isEmpty()) {
            throw new RepoServiceException(getPrettyValidationErrorMessage(itemsWithValidationErrorsByType));
        }
    }

    public Map<RepoItemType, Map<String, Schema>> getLocalRepoSchemaMap() throws RepoServiceException {
        Map<RepoItemType, Map<String, Schema>> repoItemSchemasByItemType = new HashMap<>();
        for(RepoItemType repoItemType : RepoItemType.values()) {
            Map<String, Schema> itemSchemas = new HashMap<>();
            for(RepoItem item : list(repoItemType)) {
                itemSchemas.put(NamingUtil.fromLocalToRegistrySubject(item.getName()), getSchema(item.getName()));
            }
            if(!itemSchemas.isEmpty()) {
                repoItemSchemasByItemType.put(repoItemType, itemSchemas);
            }
        }
        return repoItemSchemasByItemType;
    }

    public void validate(String name) throws RepoServiceException {
        RepoItem repoItem = getItem(name);
        validate(repoItem);
    }

    private void validate(RepoItem repoItem) throws RepoServiceException {
        parseForValidation(repoItem);
        Schema schema = getSchema(repoItem);
        validateNamespaceAndLocationMatch(schema, repoItem);
        validateLocationDepth(repoItem);
        validateEnumsHaveRequiredDefaultValue(repoItem);
    }

    private Schema getSchema(RepoItem repoItem) throws RepoServiceException {
        Schema schema;
        try {
            schema = AvroParser.schemaFromIdl(repoItem.getPathToFile(), NamingUtil.fromSubjectToFile(repoItem.getName()));
        } catch (IOException | ParseException e) {
            String error = String.format("Could not retrieve or parse schema for %s %s", repoItem.getRepoItemType().getLogLabel(), repoItem.getName());
            LOGGER.debug(error, e.getMessage());
            throw new RepoServiceException(error, e);
        }
        if(schema == null) {
            String error = String.format("An Avro record with the name %s does not exist within %s", NamingUtil.fromSubjectToFile(repoItem.getName()), repoItem.getPathToFile());
            throw new RepoServiceException(error);
        }
        return schema;
    }

    private void validateEnumsHaveRequiredDefaultValue(RepoItem repoItem) throws RepoServiceException {
        Schema schema = getSchema(repoItem);
        if(schema.getType().equals(Schema.Type.ENUM)) {
            if(!schema.hasEnumSymbol(Constants.ENUM_REQUIRED_DEFAULT_VALUE)) {
                String error = String.format("Enum %s does not have required symbol %s in its list of values", repoItem.getName(), Constants.ENUM_REQUIRED_DEFAULT_VALUE);
                throw new RepoServiceException(error);
            }
            if(!Constants.ENUM_REQUIRED_DEFAULT_VALUE.equals(schema.getEnumDefault())) {
                String error = String.format("Enum %s does not have required symbol %s listed as its default value", repoItem.getName(), Constants.ENUM_REQUIRED_DEFAULT_VALUE);
                throw new RepoServiceException(error);
            }
        }
    }

    private void validateNamespaceAndLocationMatch(Schema schema, RepoItem repoItem) throws RepoServiceException {
        String namespace = schema.getNamespace();
        Path relativePath = rootRepoPath.relativize(repoItem.getPathToFile().getParent());
        String namespaceFromPath = relativePath.toString().isEmpty() ? Constants.BASE_SCHEMA_NAMESPACE : Constants.BASE_SCHEMA_NAMESPACE + "." + relativePath.toString().replace("/",".");
        if(!namespace.equals(namespaceFromPath)) {
            String error = String.format("%s's namespace (%s) does not match its location in the local repo (%s)", repoItem.getName(), namespace, relativePath);
            throw new RepoServiceException(error);
        }
    }

    private void validateLocationDepth(RepoItem repoItem) throws RepoServiceException {
        Path relativePath = getBasePath(repoItem.getRepoItemType()).relativize(repoItem.getPathToFile().getParent());
        if(relativePath.toString().split("/").length > allowedNamespaceDepth) {
            String error = String.format("%s in its current location (%s) is nested beyond the allowed subdirectory depth of %s", repoItem.getName(), repoItem.getPathToFile(), allowedNamespaceDepth);
            throw new RepoServiceException(error);
        }
    }

    private void parseForValidation(RepoItem repoItem) throws RepoServiceException {
        try {
            AvroParser.schemaFromIdl(repoItem.getPathToFile(), NamingUtil.fromSubjectToFile(repoItem.getName()));
        } catch (ParseException | AvroRuntimeException e) {
            String error = String.format("Invalid AVDL file for %s %s: %s", repoItem.getRepoItemType().getLogLabel(), repoItem.getName(), e.getMessage());
            throw new RepoServiceException(error, e);
        } catch (IOException e) {
            String error = String.format("Could not open AVDL file for %s %s in order to parse it", repoItem.getRepoItemType().getLogLabel(), repoItem.getName());
            throw new RepoServiceException(error, e);
        }
    }

    private boolean doesItemExist(String name, RepoItemType repoItemType) throws RepoServiceException {
        Path itemPath = getItemPath(name, repoItemType);
        return (itemPath != null && Files.exists(itemPath) && !Files.isDirectory(itemPath));
    }

    private Path getItemPath(String name, RepoItemType repoItemType) throws RepoServiceException {
        return getItemPaths(repoItemType)
                .stream()
                .filter(i -> i.endsWith(NamingUtil.fromSubjectToFile(name) + Constants.AVDL_EXTENSION))
                .findFirst()
                .orElse(null);
    }

    private Path getBasePath(RepoItemType repoItemType) {
        return rootRepoPath.resolve(repoItemType.getDirectory());
    }

    private List<Path> getExcludedSubdirectoryPaths(RepoItemType repoItemType) {
        Path basePath = getBasePath(repoItemType);
        return Arrays.asList(repoItemType.getExcludedSubdirectories())
                .stream()
                .map(basePath::resolve)
                .collect(Collectors.toList());
    }

    private List<Path> getItemPaths(RepoItemType repoItemType) throws RepoServiceException {
        try {
            return FileUtil.getDescendents(getBasePath(repoItemType), getExcludedSubdirectoryPaths(repoItemType));
        } catch (IOException e) {
            String error = String.format("Could not retrieve local %s listings", repoItemType.getLogLabel());
            LOGGER.debug(error, e.getMessage());
            throw new RepoServiceException(error, e);
        }
    }

    private List<RepoItem> getItems(RepoItemType repoItemType) throws RepoServiceException {
        return getItemPaths(repoItemType).stream().map(path -> new RepoItem(getItemNameFromFilePath(path), repoItemType, path)).collect(Collectors.toList());
    }

    private String getItemNameFromFilePath(Path path) {
        return NamingUtil.fromFileToSubject(path.getFileName().toString().replace(Constants.AVDL_EXTENSION, ""));
    }

    private Map<RepoItemType, Map<String, String>> getMapOfValidationErrors() throws RepoServiceException {
        Map<RepoItemType, Map<String, String>> itemsWithValidationErrorsByType = new HashMap<>();
        for(RepoItemType repoItemType : RepoItemType.values()) {
            Map<String, String> itemsWithValidationErrors = new HashMap<>();
            for(RepoItem item : list(repoItemType)) {
                try {
                    validate(item);
                } catch (RepoServiceException e) {
                    itemsWithValidationErrors.put(item.getName(), e.getMessage());
                }
            }
            if(!itemsWithValidationErrors.isEmpty()) {
                itemsWithValidationErrorsByType.put(repoItemType, itemsWithValidationErrors);
            }
        }
        return itemsWithValidationErrorsByType;
    }

    private String getPrettyValidationErrorMessage(Map<RepoItemType, Map<String, String>> itemsWithValidationErrorsByType) {
        StringBuffer sb = new StringBuffer("Local repo has validation errors:\n\n");
        itemsWithValidationErrorsByType.entrySet().stream().forEach(repoItemTypeMapEntry -> sb.append(repoItemTypeMapEntry.getValue().entrySet().stream()
                .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n", repoItemTypeMapEntry.getKey().getDirectory() + ":\n", "\n\n"))));
        return sb.toString();
    }

}
