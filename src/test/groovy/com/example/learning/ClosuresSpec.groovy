package com.example.learning

import spock.lang.Specification

/**
 * Lesson 4: closures. This is the most important Groovy feature for Grails.
 *
 * A closure is a block of code in { } that you can store in a variable and run later,
 * like a Java lambda. Grails configuration is mostly closures, for example:
 *
 *     static constraints = {
 *         username blank: false, unique: true
 *     }
 *
 * The last test in this file shows exactly how that works.
 */
class ClosuresSpec extends Specification {

    void 'a closure is a block of code you can call later'() {
        given:
        Closure<Integer> twice = { int n -> n * 2 }    // Java: Function<Integer, Integer> twice = n -> n * 2;
        def greet = { "hello $it" }                     // only one parameter? skip naming it and call it "it"

        expect:
        twice(5) == 10
        twice.call(5) == 10          // same thing, written out in full
        greet('world') == 'hello world'
    }

    void 'when a closure is the last argument, it can go after the parentheses'() {
        given:
        def names = ['a', 'b']

        expect: 'all three lines do the same thing'
        names.collect({ it.toUpperCase() }) == ['A', 'B']
        names.collect() { it.toUpperCase() } == ['A', 'B']
        names.collect { it.toUpperCase() } == ['A', 'B']     // the style you will see everywhere
    }

    void 'closures can change variables outside them (Java lambdas cannot)'() {
        given:
        int total = 0

        when:
        [10, 20, 30].each { total += it }

        then:
        total == 60
    }

    // name: value pairs in a method call are collected into one Map argument.
    void 'named arguments arrive as a Map'() {
        when:
        def text = describe(naem: 'cat.png', size: 4500)

        then:
        text == 'cat.png (4500 bytes)'
    }

    private String describe(Map file) {
        "${file.naem} (${file.size} bytes)"
    }

    // Putting it all together: this is how Grails reads "static constraints = { ... }".
    //
    //     username blank: false, unique: true
    //
    // is a method call with the parentheses left out and named arguments, so it really means
    //
    //     username([blank: false, unique: true])
    //
    // There is no method called "username" anywhere. Grails runs the closure with a "delegate":
    // an object that receives every method call the closure can't handle itself.
    // Grails' delegate turns each call into a validation rule. Ours below just records the calls.
    void 'how a Grails DSL like "static constraints = { }" works'() {
        given: 'a closure written exactly like Grails constraints'
        def constraints = {
            username blank: false, unique: true
            password size: 8..64
        }

        and: 'an object that records every method called on it'
        def recorder = new MethodRecorder()

        when: 'we run the closure with the recorder as its delegate'
        constraints.delegate = recorder
        constraints.resolveStrategy = Closure.DELEGATE_FIRST
        constraints()

        then: 'each line was just a method call with a Map'
        recorder.calls == [
            username: [blank: false, unique: true],
            password: [size: 8..64]
        ]
    }

}

class MethodRecorder {
    Map<String, Object> calls = [:]

    // Groovy calls methodMissing whenever someone calls a method that doesn't exist.
    // name is the method's name, args holds the arguments that were passed.
    def methodMissing(String name, Object args) {
        calls[name] = args[0]
    }
}
