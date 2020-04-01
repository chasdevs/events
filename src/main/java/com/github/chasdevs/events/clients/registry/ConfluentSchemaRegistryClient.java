package com.github.chasdevs.events.clients.registry;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is an implementation of our {@link SchemaRegistryClient} that leverages Confluent's Schema Registry Client.
 * This class exists as a wrapper to their implementation, so that any potential change to their implementation does not
 * bleed into our code base's main services.
 */
@Service
public class ConfluentSchemaRegistryClient implements SchemaRegistryClient {

    private final io.confluent.kafka.schemaregistry.client.SchemaRegistryClient confluentClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluentSchemaRegistryClient.class);

    @Autowired
    public ConfluentSchemaRegistryClient(io.confluent.kafka.schemaregistry.client.SchemaRegistryClient confluentClient) {
        this.confluentClient = confluentClient;
    }

    @Override
    public Integer register(final String subject, final Schema schema) throws SchemaRegistryException {
        Integer id = null;
        try {
            id = confluentClient.register(subject, schema);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return id;
    }

    @Override
    public Schema getSchemaById(int id) throws SchemaRegistryException {
        Schema schema = null;
        try {
            schema = confluentClient.getById(id);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return schema;
    }

    @Override
    public Schema getSchemaBySubjectAndId(String subject, int id) throws SchemaRegistryException {
        Schema schema = null;
        try {
            schema = confluentClient.getBySubjectAndId(subject, id);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return schema;
    }

    @Override
    public Integer getSchemaIdBySubjectAndSchema(String subject, Schema schema) throws SchemaRegistryException {
        Integer id = null;
        try {
            id = confluentClient.getId(subject, schema);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return id;
    }

    @Override
    public SchemaMetaData getLatestSchemaMetaData(String subject) throws SchemaRegistryException {
        SchemaMetaData metaData = null;
        try {
            metaData = convertMetaData(confluentClient.getLatestSchemaMetadata(subject));
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return metaData;
    }

    @Override
    public SchemaMetaData getSchemaMetaDataBySubjectAndId(String subject, int id) throws SchemaRegistryException {
        SchemaMetaData metaData = null;
        try {
            metaData = convertMetaData(confluentClient.getSchemaMetadata(subject, id));
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return metaData;
    }

    @Override
    public Integer getSchemaVersionBySubjectAndSchema(String subject, Schema schema) throws SchemaRegistryException {
        Integer version = null;
        try {
            version = confluentClient.getVersion(subject, schema);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return version;
    }

    @Override
    public List<Integer> getAllVersionsBySubject(String subject) throws SchemaRegistryException {
        List<Integer> versions = new ArrayList<>();
        try {
            versions = confluentClient.getAllVersions(subject);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return versions;
    }

    @Override
    public Schema getLatestSchemaBySubject(String subject) throws SchemaRegistryException {
        SchemaMetaData metaData;
        metaData = getLatestSchemaMetaData(subject);
        return new Schema.Parser().parse(metaData.getSchema());
    }

    @Override
    public boolean testCompatibility(String subject, Schema schema) throws SchemaRegistryException {
        boolean compatible = false;
        try {
            compatible = confluentClient.testCompatibility(subject, schema);
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return compatible;
    }

    @Override
    public List<String> getAllSubjects() throws SchemaRegistryException {
        List<String> subjects = new ArrayList<>();
        try {
            subjects = (List<String>) confluentClient.getAllSubjects();
        } catch (IOException | RestClientException e) {
            handleExternalExceptions(e);
        }
        return subjects;
    }

    @Override
    public Map<String, Schema> getCurrentRegistrySchemaMap() throws SchemaRegistryException {
        Map<String, Schema> currentSchemasBySubject = new HashMap<>();
        for(String subject : getAllSubjects()) {
            currentSchemasBySubject.put(subject, getLatestSchemaBySubject(subject));
        }
        return currentSchemasBySubject;
    }

    private void handleExternalExceptions(Exception e) throws SchemaRegistryException {
        String errorMessage;
        if(e instanceof RestClientException && ((RestClientException) e).getStatus() == 404) {
            errorMessage = "No results found";
            throw new SchemaRegistryException(errorMessage, e);
        } else {
            errorMessage = "An error occurred when communicating with the Confluent schema registry";
            LOGGER.debug(errorMessage, e);
            throw new SchemaRegistryException(errorMessage, e);
        }
    }

    private SchemaMetaData convertMetaData(SchemaMetadata theirMetadata) {
        return SchemaMetaData.fromConfluentMetadata(theirMetadata);
    }
}
