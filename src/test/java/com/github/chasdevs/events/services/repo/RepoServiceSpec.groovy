package com.github.chasdevs.events.services.repo

import com.github.chasdevs.events.config.LocalRepoConfig
import com.github.chasdevs.events.util.NamingUtil
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class RepoServiceSpec extends Specification {

    @Subject
    RepoService repoService

    LocalRepoConfig config

    String eventName
    String commonName
    String repoRoot
    int allowedNamespaceDepth

    List<Path> createdPathsToDelete = new ArrayList<>()

    def setup() {
        repoRoot = "src/test/resources/avro"
        allowedNamespaceDepth = 2
        config = new LocalRepoConfig(repoRoot, allowedNamespaceDepth)
        eventName = "click-event"
        commonName = "http-headers"
        repoService = new RepoService(config)
    }

    def cleanup() {
        for(path in createdPathsToDelete) {
            if(Files.exists(path)) {
                Files.delete(path)
            }
        }
        createdPathsToDelete.clear()
    }

    def createConcreteImplementations() {
        repoService = new RepoService(config)
    }

    def addToCleanupList(Path path) {
        createdPathsToDelete.add(path)
    }

    def addToCleanupListWithSubdirectory(Path filePath, String subdirectory) {
        def tempPath = filePath
        def subdirParts = subdirectory.split("/")

        while(!tempPath.endsWith(subdirParts[0])) {
            tempPath = tempPath.getParent()
        }

        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .forEach({ i -> addToCleanupList(i) })
    }

    def wreckAvroIdlFile(Path path) {
        String contents = Files.readString(path)
        contents = contents.replaceAll("string", "plumbus")
        Files.writeString(path, contents)
    }

    def changeAvroRecordName(String eventName, Path path) {
        String contents = Files.readString(path)
        contents = contents.replaceAll(NamingUtil.fromSubjectToFile(eventName), "IncorrectlyNamedEvent")
        Files.writeString(path, contents)
    }

    def moveFileUpTwoLevels(Path path) {
        def filename = path.fileName
        def newLocation = path.getParent().getParent().getParent().resolve(filename)
        return Files.move(path, newLocation)
    }

    def moveCommonFileToRoot(Path path) {
        def filename = path.fileName
        def newLocation = path.getParent().getParent().resolve(filename)
        return Files.move(path, newLocation)
    }

    def pullRepoItemNameFromFullFilePath(Path path) {
        return NamingUtil.fromFileToSubject(path.getFileName().toString().replace(".avdl",""))
    }

    def "confirm specifying an invalid repo path will throw an exception"() {
        given:
            config = new LocalRepoConfig("does/not/exist", 2)
        when:
            repoService = new RepoService(config)
        then:
            thrown(IllegalArgumentException)
    }

    def "confirm attempting to get a repo item that does not exist throws an exception"() {
        given:
            createConcreteImplementations()
        when:
            def retrieved = repoService.getItem("this-does-not-exist")
        then:
            thrown(RepoServiceException)
    }

    def "confirm validating an event type with a valid Avro IDL file, but a too nested location throws an exception"() {
        given:
            createConcreteImplementations()
            def subdirectory = "content/video/too/deep"
            def currentLocation = Paths.get("src/test/resources/TooDeepTestEvent.avdl")
            def subDirPath = Files.createDirectories(Paths.get("src/test/resources/avro/" + subdirectory))
            def newLocation = Files.copy(currentLocation, subDirPath.resolve(currentLocation.getFileName()))
            addToCleanupListWithSubdirectory(newLocation, subdirectory)
        when:
            repoService.validate("too-deep-test-event")
        then:
            thrown(RepoServiceException)
    }
    
    def "confirm validating an enum with valid Avro IDL file, but without required symbol OUTDATED_SCHEMA listed in its values throws an exception"() {
        given:
            createConcreteImplementations()
            def currentLocation = Paths.get("src/test/resources/EnumWithoutSymbolOutdatedSchema.avdl")
            def newLocation = Files.copy(currentLocation, Paths.get(repoRoot).resolve(currentLocation.getFileName()))
            addToCleanupList(newLocation)
        when:
            repoService.validate("enum-without-symbol-default")
        then:
            thrown(RepoServiceException)
    }

    def "confirm validating an enum with valid Avro IDL file, but without required symbol OUTDATED_SCHEMA listed as its default value throws an exception"() {
        given:
            createConcreteImplementations()
            def currentLocation = Paths.get("src/test/resources/EnumWithoutDefaultValue.avdl")
            def newLocation = Files.copy(currentLocation, Paths.get(repoRoot).resolve(currentLocation.getFileName()))
            addToCleanupList(newLocation)
        when:
            repoService.validate("enum-without-default-value")
        then:
            thrown(RepoServiceException)
    }

    def "confirm validating a perfect enum works as expected"() {
        given:
            createConcreteImplementations()
        when:
            repoService.validate("jvm-languages")
        then:
            notThrown(RepoServiceException)
    }


}