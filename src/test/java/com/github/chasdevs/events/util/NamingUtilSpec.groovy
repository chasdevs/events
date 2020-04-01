package com.github.chasdevs.events.util

import spock.lang.Specification

class NamingUtilSpec extends Specification {

    def "confirm conversion from subject name to file name"() {
        given:
            def subject = "click-event"
            def file = "ClickEvent"
        when:
            def val = NamingUtil.fromSubjectToFile(subject)
        then:
            val == file
    }

    def "confirm conversion from file name to subject name"() {
        given:
            def file = "ClickEvent"
            def subject = "click-event"
        when:
            def val = NamingUtil.fromFileToSubject(file)
        then:
            val == subject
    }
}
