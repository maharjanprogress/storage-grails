package com.example.learning

import spock.lang.Specification

/**
 * Lesson 2: Groovy basics for a Java developer.
 *
 * Almost all Java code is also valid Groovy, so you can always fall back to
 * writing it the Java way. Groovy mostly lets you leave out the noise.
 */
class GroovyBasicsSpec extends Specification {

    void 'semicolons are optional, and the last line is returned without "return"'() {
        expect:
        shout('hi') == 'HI!'
    }

    // No "public" (it's the default) and no "return" (the last expression is returned).
    private String shout(String text) {
        text.toUpperCase() + '!'
    }

    void 'parentheses are optional when a method call is a statement on its own line'() {
        given:
        List<String> names = []

        when:
        names.add 'grails'      // same as names.add('grails')
        names.add('groovy')     // the Java way still works

        then:
        names == ['grails', 'groovy']
    }

    void 'def means "I am not writing the type"'() {
        given:
        def count = 3                  // a bit like Java's var...
        String typed = 'also fine'     // ...and you can always write the type instead

        expect:
        count instanceof Integer
        typed instanceof String

        when: '...but unlike var, a def variable can later hold a different type'
        def thing = 42
        thing = 'now a string'

        then:
        thing == 'now a string'
    }

    void 'single quotes are plain strings, double quotes can fill in values (GString)'() {
        given:
        def user = 'asha'
        def count = 3

        expect:
        'Hello $user'.contains('$')                             // single quotes: no magic at all
        "Hello $user" == 'Hello asha'                           // double quotes: $variable gets replaced
        "$user has ${count * 2} files" == 'asha has 6 files'    // ${...} can hold any expression
    }

    void 'triple quotes make multi-line strings'() {
        given:
        def json = '''{
  "name": "notes.txt"
}'''

        expect:
        json.readLines().size() == 3
    }

    // The most important difference from Java!
    void '== calls equals(), it does not compare memory addresses like Java'() {
        given:
        String a = new String('file')
        String b = new String('file')

        expect:
        a == b       // true in Groovy. In Java, a == b would be false here
        !a.is(b)     // .is() is what Java's == does: "is this the very same object?"
    }

    // In an if (...), Groovy treats "empty" values as false. This is called Groovy truth.
    // So `if (username)` checks "not null AND not empty" in one go.
    void 'Groovy truth: null, empty and zero count as false'() {
        expect: 'these are all false'
        !null
        !''
        !0
        ![]      // empty list
        ![:]     // empty map

        and: 'these are all true'
        'text'
        1
        [1]
        [a: 1]
    }

    void '?. returns null instead of throwing NullPointerException (safe navigation)'() {
        given:
        String name = null

        expect:
        name?.toUpperCase() == null       // Java would throw a NullPointerException here
        name?.trim()?.length() == null    // works across a whole chain
    }

    void '?: gives a fallback value when the left side is false-ish (Elvis operator)'() {
        given:
        String fromRequest = null
        String empty = ''

        expect:
        (fromRequest ?: 'anonymous') == 'anonymous'
        (empty ?: 'untitled.txt') == 'untitled.txt'
        ('notes.txt' ?: 'untitled.txt') == 'notes.txt'
    }

    void 'dividing two whole numbers gives a decimal, unlike Java'() {
        expect:
        5 / 2 == 2.5            // Java would give 2
        5.intdiv(2) == 2        // use intdiv() when you want Java's behaviour
        (5 / 2) instanceof BigDecimal
    }

    // Groovy is dynamic: it looks up methods while the program runs, not when it compiles.
    // That freedom is what makes Grails magic like User.findByUsername('asha') possible.
    // The price: a typo in a method name is only caught when that line runs.
    // Adding @CompileStatic to a class (see Application.groovy) makes Groovy check types like Java.
    void 'method calls are checked when the code runs, not when it compiles'() {
        given:
        def text = 'hello'

        when: 'we call a method that does not exist (this still compiles!)'
        text.fly()

        then:
        thrown(MissingMethodException)
    }
}
