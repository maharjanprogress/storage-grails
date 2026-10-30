package com.example

import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import spock.lang.Specification

/**
 * Tests the startup check in Application.setEnvironment with a hand-built Spring Environment,
 * so no app or database is started.
 */
class ApplicationSpec extends Specification {

    void 'startup stops with a helpful message when a placeholder has no value'() {
        given: 'config like application.yml, but with no DB_PASSWORD anywhere (no .env, no env var)'
        def environment = environmentWith(
            'dataSource.url': 'jdbc:postgresql://localhost:5432/storage',
            'dataSource.username': 'storage',
            'dataSource.password': '${DB_PASSWORD_THAT_IS_NOT_SET}')

        when:
        new Application().setEnvironment(environment)

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('DB_PASSWORD_THAT_IS_NOT_SET')
        e.message.contains('.env.example')
    }

    void 'startup carries on when every placeholder has a value'() {
        given: 'the placeholder AND the variable it points to, like application.yml plus .env'
        def environment = environmentWith(
            'dataSource.url': 'jdbc:postgresql://localhost:5432/storage',
            'dataSource.username': '${SOME_USER}',
            'dataSource.password': 'secret',
            'SOME_USER': 'storage')

        when:
        new Application().setEnvironment(environment)

        then:
        noExceptionThrown()
    }

    // Groovy collects named arguments (key: value, ...) into a Map, the first parameter
    private static StandardEnvironment environmentWith(Map<String, Object> properties) {
        def environment = new StandardEnvironment()
        // addFirst: highest precedence, so these win over any real environment variables
        environment.propertySources.addFirst(new MapPropertySource('test', properties))
        environment
    }
}
