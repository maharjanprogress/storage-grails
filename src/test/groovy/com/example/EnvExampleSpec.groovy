package com.example

import spock.lang.Specification

/**
 * Keeps .env.example honest: every ${VARIABLE} that application.yml or compose.dev.yml needs
 * must be listed in .env.example, otherwise a new teammate's copy of it won't have it.
 *
 * Gradle runs tests with the project folder as the working directory, so plain relative
 * paths like new File('.env.example') work here.
 */
class EnvExampleSpec extends Specification {

    // Variables used with a default (${APP_PORT:-9030} in compose, ${X:y} in Spring) are optional.
    // This matches only placeholders WITHOUT a default: ${NAME}
    private static final REQUIRED_PLACEHOLDER = /\$\{([A-Z][A-Z0-9_]*)\}/

    void 'every required variable in #file is listed in .env.example'() {
        given:
        Set<String> documented = envExampleKeys()

        when:
        // Both files are YAML, where "#" starts a comment. Comments may mention ${NAME} as an
        // example, so only real (non-comment) lines count.
        def code = new File(file).readLines().findAll { !it.trim().startsWith('#') }.join('\n')
        def used = (code =~ REQUIRED_PLACEHOLDER).collect { it[1] } as Set

        then:
        // used - documented: Groovy's minus on collections, i.e. "used but not documented"
        (used - documented).isEmpty()

        where:
        file << ['grails-app/conf/application.yml', 'compose.dev.yml']
    }

    void '.env is gitignored, so real passwords are never committed'() {
        expect:
        new File('.gitignore').readLines()*.trim().contains('.env')
    }

    private static Set<String> envExampleKeys() {
        new File('.env.example').readLines()
            .findAll { it.contains('=') && !it.startsWith('#') }
            .collect { it.split('=', 2)[0].trim() } as Set
    }
}
