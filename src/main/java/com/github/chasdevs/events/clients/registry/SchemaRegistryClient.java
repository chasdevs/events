package com.github.chasdevs.events.clients.registry;

import org.apache.avro.Schema;

import java.util.List;
import java.util.Map;

/**
 * Registry client interface mostly mirroring Confluent's schema registry client's interface.
 */
public interface SchemaRegistryClient {

    Integer register(String subject, Schema schema) throws SchemaRegistryException;

    Schema getSchemaById(int id) throws SchemaRegistryException;
    Schema getSchemaBySubjectAndId(String subject, int id) throws SchemaRegistryException;
    Integer getSchemaIdBySubjectAndSchema(String subject, Schema schema) throws SchemaRegistryException;

    SchemaMetaData getLatestSchemaMetaData(String subject) throws SchemaRegistryException;
    SchemaMetaData getSchemaMetaDataBySubjectAndId(String subject, int id) throws SchemaRegistryException;

    Integer getSchemaVersionBySubjectAndSchema(String subject, Schema schema) throws SchemaRegistryException;
    List<Integer> getAllVersionsBySubject(String subject) throws SchemaRegistryException;

    Schema getLatestSchemaBySubject(String subject) throws SchemaRegistryException;

    boolean testCompatibility(String subject, Schema schema) throws SchemaRegistryException;

    List<String> getAllSubjects() throws SchemaRegistryException;

    Map<String, Schema> getCurrentRegistrySchemaMap() throws SchemaRegistryException;

}
