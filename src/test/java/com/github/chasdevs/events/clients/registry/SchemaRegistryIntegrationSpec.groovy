package com.github.chasdevs.events.clients.registry


import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import spock.lang.Specification
import spock.lang.Subject

class SchemaRegistryIntegrationSpec extends Specification {

    @Subject
    ConfluentSchemaRegistryClient registryClient

    def setup() {
        CachedSchemaRegistryClient theirClient = new CachedSchemaRegistryClient("http://localhost:8081", 512);
        registryClient = new ConfluentSchemaRegistryClient(theirClient)
    }

    def "confirm interactions while getting schema by ID"() {
        when:
            registryClient.getAllSubjects()
        then:
            true
    }

}
