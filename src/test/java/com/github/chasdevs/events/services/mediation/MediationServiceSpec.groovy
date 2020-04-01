package com.github.chasdevs.events.services.mediation

import com.github.chasdevs.events.clients.registry.SchemaRegistryClient
import com.github.chasdevs.events.clients.registry.SchemaRegistryException
import com.github.chasdevs.events.services.repo.RepoItemType

import com.github.chasdevs.events.services.repo.RepoService
import com.github.chasdevs.events.util.NamingUtil
import com.github.chasdevs.events.util.TestUtility
import org.apache.avro.Schema
import spock.lang.Specification
import spock.lang.Subject

class MediationServiceSpec extends Specification{

    @Subject
    MediationService mediationService

    RepoService repoService
    SchemaRegistryClient registryClient

    Schema schema

    String eventName

    def setup() {
        repoService = Mock()
        registryClient = Mock()
        mediationService = new MediationService(registryClient, repoService)
        schema = TestUtility.schemaFixtureData.get()
        eventName = "click-event"
    }

    def cleanup() {
    }

    def "confirm interactions when syncing local with matching remote"() {
        given:
            def registryMap = [(eventName):schema]
            def localMap = [(RepoItemType.EVENT):[(eventName):schema]]
        when:
            def resultsMap = mediationService.syncLocalWithRemote()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            resultsMap.isEmpty()
    }

    def "confirm interactions when syncing local with non-matching remote, existing event updated"() {
        given:
            def updatedSchema = TestUtility.updatedSchemaFixtureData.get()
            def registryMap = [(eventName):schema]
            def localMap = [(RepoItemType.EVENT):[(eventName):updatedSchema]]
        when:
            def resultsMap = mediationService.syncLocalWithRemote()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            resultsMap.isEmpty()
    }

    def "confirm interactions when syncing local with non-matching remote, new event created"() {
        given:
            def registryMap = [:]
            def localMap = [(RepoItemType.EVENT):[(eventName):schema]]
        when:
            def resultsMap = mediationService.syncLocalWithRemote()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            resultsMap.isEmpty()
    }

    def "confirm interactions when testing compatibility of local repo with matching remote"() {
        given:
            def registryMap = [(eventName):schema]
            def localMap = [(RepoItemType.EVENT):[(eventName):schema]]
        when:
            def resultsMap = mediationService.testGlobalCompatibility()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            0 * registryClient.testCompatibility(_, _)
            resultsMap.isEmpty()
    }

    def "confirm interactions when testing compatibility of local repo with non-matching, incompatible remote"() {
        given:
            def updatedSchema = TestUtility.updatedSchemaFixtureData.get()
            def registryMap = [(eventName):schema]
            def localMap = [(RepoItemType.EVENT):[(eventName):updatedSchema]]
        when:
            def resultsMap = mediationService.testGlobalCompatibility()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            1 * registryClient.testCompatibility(eventName, updatedSchema) >> false
            thrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of local repo with non-matching, compatible remote"() {
        given:
            def updatedSchema = TestUtility.updatedSchemaFixtureData.get()
            def registryMap = [(eventName):schema]
            def localMap = [(RepoItemType.EVENT):[(eventName):updatedSchema]]
        when:
            def resultsMap = mediationService.testGlobalCompatibility()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            1 * registryClient.testCompatibility(eventName, updatedSchema) >> true
            resultsMap.isEmpty()
            notThrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of local repo with schema that doesn't exist in remote"() {
        given:
            def registryMap = [:]
            def localMap = [(RepoItemType.EVENT):[(eventName):schema]]
        when:
            def resultsMap = mediationService.testGlobalCompatibility()
        then:
            1 * repoService.getLocalRepoSchemaMap() >> localMap
            1 * registryClient.getCurrentRegistrySchemaMap() >> registryMap
            0 * registryClient.testCompatibility(eventName, _)
            resultsMap.isEmpty()
            notThrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of a single event that is the same as its remote counterpart"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
        when:
            def results = mediationService.testCompatibility(eventName)
        then:
            1 * repoService.getSchema(eventName) >> schema
            1 * registryClient.getLatestSchemaBySubject(registrySubjectName) >> schema
            0 * registryClient.testCompatibility(registrySubjectName, schema)
            notThrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of a single event that is compatible"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
            def updatedSchema = TestUtility.updatedSchemaFixtureData.get()
        when:
            def results = mediationService.testCompatibility(eventName)
        then:
            1 * repoService.getSchema(eventName) >> updatedSchema
            1 * registryClient.getLatestSchemaBySubject(registrySubjectName) >> schema
            1 * registryClient.testCompatibility(registrySubjectName, updatedSchema) >> true
            notThrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of a single event that is incompatible"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
            def updatedSchema = TestUtility.updatedSchemaFixtureData.get()
        when:
            def results = mediationService.testCompatibility(eventName)
        then:
            1 * repoService.getSchema(eventName) >> updatedSchema
            1 * registryClient.getLatestSchemaBySubject(registrySubjectName) >> schema
            1 * registryClient.testCompatibility(registrySubjectName, updatedSchema) >> false
            thrown(MediationServiceException)
    }

    def "confirm interactions when testing compatibility of a single event that doesn't exist in remote"() {
        given:
            def registrySubjectName = NamingUtil.fromLocalToRegistrySubject(eventName)
        when:
            def results = mediationService.testCompatibility(eventName)
        then:
            1 * repoService.getSchema(eventName) >> schema
            1 * registryClient.getLatestSchemaBySubject(registrySubjectName) >> {
                throw new SchemaRegistryException("No results found")
            }
            0 * registryClient.testCompatibility(registrySubjectName, schema)
            thrown(MediationServiceException)
    }

    def "verify output of getPrettyMessageFromCompatibilityResultsMap"() {
        given:
            def resultsMap = Map.of(RepoItemType.EVENT, Map.of("test-event", "Bad schema yo") as Map<String, String>)
        when:
            def results = mediationService.getPrettyMessageFromCompatibilityResultsMap(resultsMap)
        then:
            results == "Compatibility check completed with some failures. Updated schemas and their compatibility test results:\n\n    test-event:\n        Bad schema yo"
    }

}
