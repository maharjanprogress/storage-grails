package com.example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import spock.lang.Specification

import javax.sql.DataSource

/**
 * The same User class, but saved through real Hibernate into a real SQL table (H2 in the
 * "test" environment until Round 9). UserSpec can't check these things: it never runs SQL.
 *
 * @Rollback runs each test in a transaction that is rolled back at the end, so tests don't
 * see each other's rows. Spring Boot equivalent: @Transactional on a test (rolls back by default).
 */
@Integration
@Rollback
class UserPersistenceIntegrationSpec extends Specification {

    // Integration specs are Spring beans, so they can receive beans too (by name, like BootStrap)
    DataSource dataSource

    void 'a saved user ends up as a row in the "users" table'() {
        given:
        def user = new User(username: 'alice', passwordHash: 'hash').save(flush: true, failOnError: true)

        when:
        // groovy.sql.Sql is Groovy's small JDBC helper (think JdbcTemplate). The GString's
        // ${...} values become "?" parameters, NOT pasted into the SQL, so this is injection-safe.
        def row = new Sql(dataSource).firstRow("select username, password_hash from users where id = ${user.id}")

        then: 'the table is "users" (from static mapping) and passwordHash became the column password_hash'
        row.username == 'alice'
        row.password_hash == 'hash'
    }

    void 'the database itself refuses a duplicate username, even if validation is skipped'() {
        given:
        new User(username: 'alice', passwordHash: 'hash1').save(flush: true, failOnError: true)

        when: 'validate: false skips the unique check, so the INSERT reaches the database'
        new User(username: 'alice', passwordHash: 'hash2').save(flush: true, validate: false)

        then: 'unique: true also created a UNIQUE constraint in the schema, which is what catches it'
        // The constraint alone has a race: two registrations at the same moment can both pass
        // the "is it taken?" query. The database constraint is the real guarantee.
        thrown(org.springframework.dao.DataIntegrityViolationException)
    }

    void 'dynamic finders run real SQL queries'() {
        given:
        ['alice', 'alex', 'bob'].each { new User(username: it, passwordHash: 'hash').save(flush: true, failOnError: true) }

        expect:
        User.findByUsername('alice').passwordHash == 'hash'
        User.countByUsernameLike('al%') == 2
    }
}
