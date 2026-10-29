package com.example.learning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import spock.lang.Specification

/**
 * Lesson 5: classes, properties and traits.
 *
 * Grails domain classes (round 4) are ordinary Groovy classes, so everything here
 * applies to them directly.
 */
class ClassesSpec extends Specification {

    void 'a field without "private" becomes a property with a getter and setter'() {
        given:
        def file = new UploadedFile()

        when:
        file.name = 'cat.png'          // really calls setName('cat.png')

        then:
        file.name == 'cat.png'         // really calls getName()
        file.getName() == 'cat.png'    // the generated getter does exist
    }

    void 'every class gets a named-argument constructor for free'() {
        when:
        def file = new UploadedFile(name: 'cat.png', sizeInBytes: 4500)

        then:
        file.name == 'cat.png'
        file.sizeInBytes == 4500
    }

    void 'parameters can have default values'() {
        given:
        def file = new UploadedFile(name: 'cat.png', sizeInBytes: 2048)

        expect:
        file.sizeIn() == '2 KB'        // unit falls back to its default, 'KB'
        file.sizeIn('B') == '2048 B'
    }

    // Annotations like these write boilerplate code for you while compiling, similar to Lombok.
    void '@ToString and @EqualsAndHashCode generate the usual methods'() {
        given:
        def a = new UploadedFile(name: 'cat.png', sizeInBytes: 4500)
        def b = new UploadedFile(name: 'cat.png', sizeInBytes: 4500)

        expect:
        a == b                 // uses the generated equals()
        a.toString() == 'UploadedFile(name:cat.png, sizeInBytes:4500)'
    }

    void 'multiple assignment unpacks a list into variables'() {
        when:
        def (baseName, extension) = 'cat.png'.tokenize('.')

        then:
        baseName == 'cat'
        extension == 'png'
    }

    // A trait is an interface that can also hold fields and real methods.
    // Grails uses traits a lot. In round 5 you'll write
    //     class UserServiceSpec extends Specification implements ServiceUnitTest<UserService>
    // and the ServiceUnitTest trait gives your test a ready-made "service" property.
    void 'a trait adds fields and methods to any class that implements it'() {
        given:
        def folder = new SharedFolder(name: 'holiday')

        when:
        folder.shareWith('ram')
        folder.shareWith('sita')

        then:
        folder.sharedWith == ['ram', 'sita']
    }

    void '@CompileStatic code behaves the same, it is just type-checked like Java'() {
        expect:
        SizeFormatter.megabytes(3_145_728) == '3 MB'
    }
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class UploadedFile {
    String name           // no "private", so Groovy generates getName() and setName()
    long sizeInBytes

    String sizeIn(String unit = 'KB') {
        unit == 'KB' ? "${sizeInBytes.intdiv(1024)} KB" : "$sizeInBytes B"
    }
}

trait Shareable {
    List<String> sharedWith = []

    void shareWith(String username) {
        sharedWith << username
    }
}

class SharedFolder implements Shareable {
    String name
}

@CompileStatic
class SizeFormatter {
    static String megabytes(long bytes) {
        // Exercise: uncomment the next line and build. With @CompileStatic it's a compile error.
        // Remove @CompileStatic and it compiles, then fails only when the line runs.
        // bytes.fly()
        "${bytes.intdiv(1024 * 1024)} MB"
    }
}
