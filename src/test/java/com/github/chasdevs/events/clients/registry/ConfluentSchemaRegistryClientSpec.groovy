package com.github.chasdevs.events.clients.registry

import com.github.chasdevs.events.util.TestUtility
import io.confluent.kafka.schemaregistry.client.SchemaMetadata
import org.apache.avro.Schema
import spock.lang.Specification
import spock.lang.Subject

class ConfluentSchemaRegistryClientSpec extends Specification {

    @Subject
    ConfluentSchemaRegistryClient registryClient

    io.confluent.kafka.schemaregistry.client.SchemaRegistryClient theirClient

    Schema schema
    String subject
    int id
    int version

    def setup() {
        theirClient = Mock()
        registryClient = new ConfluentSchemaRegistryClient(theirClient)
        subject = "click-event"
        id = 7
        version = 1
        schema = TestUtility.schemaFixtureData.get()
    }

    def cleanup() {
    }

    def "confirm interactions while registering"() {
        when:
            registryClient.register(subject, schema)
        then:
            1 * theirClient.register(subject, schema)
    }

    def "confirm interactions while getting schema by ID"() {
        when:
            registryClient.getSchemaById(id)
        then:
            1 * theirClient.getById(id)
    }

    def "confirm interactions while getting schema by subject and ID"() {
        when:
            registryClient.getSchemaBySubjectAndId(subject, id)
        then:
            1 * theirClient.getBySubjectAndId(subject, id)
    }

    def "confirm interactions while getting schema ID by subject and schema"() {
        when:
            registryClient.getSchemaIdBySubjectAndSchema(subject, schema)
        then:
            1 * theirClient.getId(subject, schema)
    }

    def "confirm interactions while getting latest schema meta data by subject"() {
        given:
            def theirMetaData = new SchemaMetadata(id, version, schema.toString())
        when:
            def result = registryClient.getLatestSchemaMetaData(subject)
        then:
            1 * theirClient.getLatestSchemaMetadata(subject) >> theirMetaData
            result == SchemaMetaData.fromConfluentMetadata(theirMetaData)
    }

    def "confirm interactions while getting schema meta data by subject and id"() {
        given:
            def theirMetaData = new SchemaMetadata(id, version, schema.toString())
        when:
            def result = registryClient.getSchemaMetaDataBySubjectAndId(subject, id)
        then:
            1 * theirClient.getSchemaMetadata(subject, id) >> theirMetaData
            result == SchemaMetaData.fromConfluentMetadata(theirMetaData)
    }

    def "confirm interactions while getting schema version by subject and schema"() {
        when:
            registryClient.getSchemaVersionBySubjectAndSchema(subject, schema)
        then:
            1 * theirClient.getVersion(subject, schema)
    }

    def "confirm interactions while getting all versions by subject"() {
        when:
            registryClient.getAllVersionsBySubject(subject)
        then:
            1 * theirClient.getAllVersions(subject)

    }

    def "confirm interactions while testing schema compatibility by subject and schema"() {
        when:
            registryClient.testCompatibility(subject, schema)
        then:
            1 * theirClient.testCompatibility(subject, schema)
    }

    def "confirm interactions while getting all subjects"() {
        when:
            registryClient.getAllSubjects()
        then:
            1 * theirClient.getAllSubjects()
    }

    def "confirm interactions while getting latest schema by subject"() {
        given:
            def theirMetaData = new SchemaMetadata(id, version, schema.toString())
        when:
            def returnedSchema = registryClient.getLatestSchemaBySubject(subject)
        then:
            1 * theirClient.getLatestSchemaMetadata(subject) >> theirMetaData
            returnedSchema == schema
    }

    def "confirm interactions while getting current registry schema map"() {
        given:
            def subjects = ["click-event"]
            def theirMetaData = new SchemaMetadata(id, version, schema.toString())
        when:
            def map = registryClient.getCurrentRegistrySchemaMap()
        then:
            1 * theirClient.getAllSubjects() >> subjects
            1 * theirClient.getLatestSchemaMetadata(subject) >> theirMetaData
            map == [(subject): schema]
    }

}
