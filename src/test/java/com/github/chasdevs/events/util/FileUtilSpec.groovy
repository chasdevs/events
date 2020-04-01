package com.github.chasdevs.events.util

import spock.lang.Specification

import java.nio.file.Path

class FileUtilSpec extends Specification {

    def "confirm file util returns correct number of schema paths"() {
        given:
            def pathToTest = Path.of("src/test/resources/avro")
            def exclusions = new ArrayList()
        when:
            def paths = FileUtil.getDescendents(pathToTest, exclusions)
        then:
            paths != null
            paths instanceof ArrayList
            paths.size() == 1
    }
}
