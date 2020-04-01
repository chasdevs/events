package com.github.chasdevs.events.command

import com.github.chasdevs.events.clients.registry.SchemaRegistryClient
import com.github.chasdevs.events.config.SchemaRegistryConfig
import com.github.chasdevs.events.services.mediation.MediationService
import com.github.chasdevs.events.services.repo.RepoItemType
import com.github.chasdevs.events.services.repo.RepoService
import com.github.chasdevs.events.util.NamingUtil
import org.apache.avro.Schema
import spock.lang.Specification
import spock.lang.Subject

class SchemaCommandsSpec extends Specification{

    @Subject
    SchemaCommands eventCommands

    RepoService repoService
    SchemaRegistryClient registryClient
    SchemaRegistryConfig registryConfig
    MediationService mediationService

    String eventName = "click-event"

    def setup() {
        repoService = Mock()
        registryClient = Mock()
        registryConfig = Mock()
        mediationService = Mock()
        eventCommands = new SchemaCommands(repoService, registryClient, registryConfig, mediationService)
    }

    def cleanup() {
    }
    
    def "confirm interactions when printing a schema locally"() {
        when:
            eventCommands.print(eventName, false)
        then:
            1 * repoService.getSchema(eventName) >> Schema.create(Schema.Type.BOOLEAN)
            0 * registryClient.getLatestSchemaBySubject(_)
    }

    def "confirm interactions when printing a schema remotely"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
        when:
            eventCommands.print(eventName, true)
        then:
            1 * registryClient.getLatestSchemaBySubject(registrySubjectName) >> Schema.create(Schema.Type.BOOLEAN)
            0 * repoService.getSchema(_)
    }

    def "confirm interactions when registering schemas"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
            def schemaToRegister = Schema.create(Schema.Type.BOOLEAN)
        when:
            eventCommands.register(eventName)
        then:
            1 * repoService.getSchema(eventName) >> schemaToRegister
            1 * registryClient.register(registrySubjectName, schemaToRegister)
    }

    def "confirm interactions when validating an event"() {
        when:
            eventCommands.validate(eventName)
        then:
            1 * repoService.validate(eventName)
            0 * repoService.validateLocalRepo()
    }

    def "confirm interactions when validating the local repo"() {
        when:
            eventCommands.validate(null)
        then:
            1 * repoService.validateLocalRepo()
            0 * repoService.validate(_)
    }

    def "confirm interactions when syncing the local repo with the schema registry correctly"() {
        given:
            def resultsMap = [(RepoItemType.EVENT):["click-event":true]]
        when:
            eventCommands.sync(true)
        then:
            1 * mediationService.syncLocalWithRemote() >> resultsMap
            1 * mediationService.getPrettyMessageFromSyncResultsMap(resultsMap)
    }

    def "confirm interactions when syncing the local repo without the force flag"() {
        when:
            eventCommands.sync(false)
        then:
            0 * mediationService.syncLocalWithRemote()
            0 * mediationService.getPrettyMessageFromSyncResultsMap(_)
    }

    def "confirm interactions when testing compatibility of an event"() {
        when:
            eventCommands.testCompatibility(eventName)
        then:
            1 * mediationService.testCompatibility(eventName)
            0 * mediationService.testGlobalCompatibility()
    }

    def "confirm interactions when testing the compatibility of the local repo"() {
        given:
            def resultsMap = [(RepoItemType.EVENT):["click-event":true]]
        when:
            eventCommands.testCompatibility(null)
        then:
            1 * mediationService.testGlobalCompatibility() >> resultsMap
            1 * mediationService.getPrettyMessageFromCompatibilityResultsMap(resultsMap)
            0 * mediationService.testCompatibility(_)
    }

}
