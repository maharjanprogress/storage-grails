package com.example.learning

import spock.lang.Specification

/**
 * Lesson 1: how a Spock test is built.
 *
 * Spock is the test framework Grails uses. A test class is called a
 * "specification" and extends Specification. Each test method is a
 * "feature method", and its name is a plain string so it reads like a sentence.
 *
 * Spock runs on top of JUnit 5, so IntelliJ and Gradle run these exactly like JUnit tests.
 */
class SpockBasicsSpec extends Specification {

    // expect: is for simple checks. Every line in it must be true.
    // There is no assertEquals: a plain true/false expression IS the assertion.
    void 'expect: checks that each line is true'() {
        expect:
        1 + 1 == 2
        'grails'.toUpperCase() == 'GRAILS'
    }

    // given: set things up    (JUnit: "arrange")
    // when:  do the action     (JUnit: "act")
    // then:  check the result  (JUnit: "assert")
    // The text after each label is optional and only there for humans.
    void 'given / when / then splits a test into three steps'() {
        given: 'an empty list'
        List<String> files = []

        when: 'we add a file name'
        files.add('photo.png')

        then: 'the list holds exactly that file'
        files.size() == 1
        files[0] == 'photo.png'
    }

    // where: runs the same test once per row of the table (JUnit: @ParameterizedTest).
    // Inputs go left of ||, expected results go right of it.
    // #word and #expected in the name are filled in, so each row shows up as its own test.
    void 'the length of "#word" is #expected'() {
        expect:
        word.length() == expected

        where:
        word     || expected
        'cat'    || 3
        'groovy' || 6
        ''       || 0
    }

    // thrown() checks that the when: block threw (JUnit: assertThrows).
    void 'thrown() checks that an exception happened'() {
        when:
        Integer.parseInt('not a number')

        then:
        NumberFormatException e = thrown()
        e.message.contains('not a number')
    }

    // Spock has mocking built in, so you rarely need Mockito.
    // Stub() is a fake that returns canned answers   (Mockito: when(...).thenReturn(...))
    // ">>" means "returns".
    void 'a Stub returns canned answers'() {
        given:
        WallClock clock = Stub()
        clock.currentHour() >> 9

        expect:
        new Greeter(clock).greet('Asha') == 'Good morning, Asha'
    }

    // Mock() is a fake that also records how it was called (Mockito: verify(...)).
    // "1 * notifier.send(...)" means: send was called exactly once, with this argument.
    void 'a Mock checks how many times a method was called'() {
        given:
        Notifier notifier = Mock()
        def uploader = new Uploader(notifier)

        when:
        uploader.upload('report.pdf')

        then:
        1 * notifier.send('Uploaded report.pdf')
    }
}

// Small classes used by the tests above. A Groovy file can hold several classes.
// In the real app, classes like these live in their own files under grails-app/ or src/main/groovy.

interface WallClock {
    int currentHour()
}

class Greeter {
    private final WallClock clock

    Greeter(WallClock clock) {
        this.clock = clock
    }

    String greet(String name) {
        clock.currentHour() < 12 ? "Good morning, $name" : "Good afternoon, $name"
    }
}

interface Notifier {
    void send(String message)
}

class Uploader {
    private final Notifier notifier

    Uploader(Notifier notifier) {
        this.notifier = notifier
    }

    void upload(String fileName) {
        // pretend the file gets saved here
        notifier.send("Uploaded $fileName")
    }
}
