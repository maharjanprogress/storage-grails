package com.example.learning

import spock.lang.Specification

/**
 * Lesson 3: lists, maps and ranges.
 *
 * You will use these constantly in Grails. For example, a controller sends JSON with
 *     render([name: file.name, size: file.size] as JSON)
 * and that [name: ..., size: ...] is just a map.
 */
class CollectionsSpec extends Specification {

    void 'list and map literals'() {
        given:
        def sizes = [120, 4500, 80]                  // a java.util.ArrayList
        def file = [name: 'cat.png', size: 4500]     // a java.util.LinkedHashMap with String keys

        expect:
        sizes instanceof ArrayList
        sizes[0] == 120
        sizes[-1] == 80               // a negative index counts from the end

        file instanceof LinkedHashMap
        file.name == 'cat.png'        // read a key as if it were a property...
        file['size'] == 4500          // ...or with square brackets
    }

    void 'adding to lists and maps'() {
        given:
        def names = ['a.txt']
        def file = [name: 'a.txt']

        when:
        names << 'b.txt'              // << appends, same as names.add('b.txt')
        file.type = 'text/plain'      // writing to a new key adds it

        then:
        names == ['a.txt', 'b.txt']   // == compares the contents
        file == [name: 'a.txt', type: 'text/plain']
    }

    // These methods replace most Java stream code. Each one takes a closure (the { } block),
    // and "it" is the current item. Closures are explained properly in the next lesson.
    void 'collect, findAll, find and friends replace Java streams'() {
        given:
        def files = [
            [name: 'cat.png', size: 4500, owner: 'asha'],
            [name: 'notes.txt', size: 120, owner: 'ram'],
            [name: 'report.pdf', size: 9000, owner: 'asha']
        ]

        expect: 'collect = stream().map()'
        files.collect { it.name } == ['cat.png', 'notes.txt', 'report.pdf']

        and: 'findAll = stream().filter()'
        files.findAll { it.owner == 'asha' }.size() == 2

        and: 'find = the first match, or null when nothing matches'
        files.find { it.size > 5000 }.name == 'report.pdf'
        files.find { it.size > 99999 } == null

        and: 'sum, max, any and every'
        files.sum { it.size } == 13620
        files.max { it.size }.name == 'report.pdf'
        files.any { it.name.endsWith('.txt') }
        files.every { it.size > 0 }
    }

    void 'groupBy turns a list into a map of lists'() {
        given:
        def files = [
            [name: 'cat.png', owner: 'asha'],
            [name: 'notes.txt', owner: 'ram'],
            [name: 'report.pdf', owner: 'asha']
        ]

        when:
        def byOwner = files.groupBy { it.owner }

        then:
        byOwner.keySet() == ['asha', 'ram'] as Set
        byOwner.asha*.name == ['cat.png', 'report.pdf']
    }

    // *. calls the same thing on every item and gives back a list of the results.
    void 'the spread operator *. works on every item'() {
        given:
        def names = ['cat.png', 'notes.txt']

        expect:
        names*.toUpperCase() == ['CAT.PNG', 'NOTES.TXT']
        names*.length() == [7, 9]
    }

    void 'ranges and the "in" operator'() {
        given:
        def workingHours = 9..17       // 9 up to and including 17
        def okStatuses = [200, 201, 204]

        expect:
        12 in workingHours
        20 !in workingHours
        201 in okStatuses
        (1..3).collect { it * 10 } == [10, 20, 30]
    }

    void 'looping without a for statement'() {
        given:
        def lines = []

        when:
        [a: 1, b: 2].each { key, value -> lines << "$key=$value" }
        ['x', 'y'].eachWithIndex { item, i -> lines << "$i:$item" }
        3.times { lines << 'tick' }

        then:
        lines == ['a=1', 'b=2', '0:x', '1:y', 'tick', 'tick', 'tick']
    }
}
