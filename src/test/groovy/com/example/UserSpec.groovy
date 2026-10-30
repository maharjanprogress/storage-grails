package com.example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * A domain unit test. DomainUnitTest<User> sets up an IN-MEMORY GORM for User (no Hibernate,
 * no database), so save(), validate() and dynamic finders all work and run in milliseconds.
 * Spring Boot's closest equivalent is @DataJpaTest, though that one does start a database.
 *
 * Real SQL against a real table is checked in UserPersistenceIntegrationSpec.
 */
class UserSpec extends Specification implements DomainUnitTest<User> {

    // ---- Constraints and validate() ---------------------------------------------------------

    void 'a username of "#username" is #outcome'() {
        given:
        def user = new User(username: username, passwordHash: 'hash')

        expect:
        // validate() runs the constraints and fills in user.errors. It returns true when valid.
        // validate(['username']) would check just that one property.
        user.validate() == (errorCode == null)
        // errors works like Spring's BindingResult. Each failure has a code naming the rule
        // that broke, e.g. "size.toosmall". (Groovy's ?. stops at null instead of throwing.)
        user.errors.getFieldError('username')?.code == errorCode

        where:
        username         || errorCode
        'alice'          || null
        'bob_99'         || null
        'abc'            || null                // exactly the 3-character minimum
        'a' * 30         || null                // Groovy can multiply a String: 30 "a"s
        null             || 'nullable'          // because of nullable: false (Grails 8 would allow it otherwise)
        ''               || 'nullable'          // not 'blank'! See the next test for why
        'ab'             || 'size.toosmall'
        'a' * 31         || 'size.toobig'
        'Alice'          || 'matches.invalid'   // uppercase isn't allowed
        'alice smith'    || 'matches.invalid'
        'alice@example'  || 'matches.invalid'

        outcome = errorCode ? "rejected ($errorCode)" : 'accepted'  // computed column, used in the test name
    }

    void 'the map constructor turns empty strings into null, so blank only fires on a direct set'() {
        // new User(username: '') doesn't just set the field: it goes through Grails "data binding",
        // the same code that copies request parameters into objects (like Spring's @ModelAttribute
        // binding). Data binding converts '' to null by default, so the error is 'nullable'.
        // Setting the property directly skips data binding, and then the blank rule fires.
        when:
        def bound = new User(username: 'alice', passwordHash: '')
        def assigned = new User(username: 'alice')
        assigned.passwordHash = ''

        then:
        bound.passwordHash == null
        !bound.validate()
        bound.errors.getFieldError('passwordHash').code == 'nullable'

        assigned.passwordHash == ''
        !assigned.validate()
        assigned.errors.getFieldError('passwordHash').code == 'blank'
    }

    void 'Grails 8: a property with no nullable: false would accept null'() {
        // dateCreated has no constraints at all, yet it isn't listed as nullable, because GORM
        // fills it in itself. Our two strings are only required because they say nullable: false.
        expect:
        !User.constrainedProperties.username.nullable
        !User.constrainedProperties.passwordHash.nullable
    }

    // ---- save() -----------------------------------------------------------------------------

    void 'save() on an invalid object returns null instead of throwing'() {
        // Surprise for Spring developers: JPA's repository.save() would throw on invalid data.
        // GORM's save() validates first and, if that fails, quietly returns null and leaves the
        // reasons in errors. Always check the result (or call save(failOnError: true)).
        when:
        def result = new User(username: 'x', passwordHash: 'hash').save()

        then:
        result == null
        User.count() == 0
    }

    void 'save(failOnError: true) throws a ValidationException instead'() {
        when:
        new User(username: 'x', passwordHash: 'hash').save(failOnError: true)

        then:
        thrown(grails.validation.ValidationException)
    }

    void 'save() gives the object an id and fills in the timestamps'() {
        when:
        // flush: true writes to the database now instead of at the end of the session
        // (Hibernate batches writes, like JPA's saveAndFlush vs save)
        def user = new User(username: 'alice', passwordHash: 'hash').save(flush: true)

        then:
        user.id != null
        user.dateCreated != null
        user.lastUpdated != null
    }

    void 'usernames must be unique'() {
        given:
        new User(username: 'alice', passwordHash: 'hash1').save(flush: true)

        when:
        def duplicate = new User(username: 'alice', passwordHash: 'hash2')

        then:
        // unique: true makes validate() run a query to look for an existing "alice"
        !duplicate.validate()
        duplicate.errors.getFieldError('username').code == 'unique'
        duplicate.save() == null
    }

    // ---- Reading: get, dynamic finders, count ----------------------------------------------

    void 'dynamic finders are made up from property names'() {
        given:
        ['alice', 'alex', 'bob'].each { new User(username: it, passwordHash: 'hash').save(flush: true) }

        expect:
        // None of these methods are written anywhere. GORM reads the method NAME when it's called
        // (Groovy's methodMissing, Round 1) and turns it into a query, just like Spring Data does
        // with the method names you declare in a repository interface.
        User.findByUsername('alice').username == 'alice'
        User.findByUsername('nobody') == null                          // find... returns null if nothing matches
        User.findAllByUsernameLike('al%')*.username.sort() == ['alex', 'alice']
        User.countByUsernameLike('al%') == 2
        User.count() == 3
        User.get(User.findByUsername('bob').id).username == 'bob'      // get(id) ↔ findById(id)
    }

    void 'a typo in a dynamic finder fails when it runs, not when it compiles'() {
        // The flip side of methods made up at runtime: the compiler can't check the name.
        // Spring Data checks derived query names when the app starts; GORM only when it's called.
        when: 'the property name is misspelled: GORM still parses it as a finder, then finds no "usernam"'
        User.findByUsernam('alice')

        then:
        thrown(org.springframework.dao.InvalidDataAccessResourceUsageException)

        when: 'the name isn\'t a finder at all, like existsByUsername (Spring Data has it, GORM doesn\'t)'
        User.existsByUsername('alice')

        then:
        thrown(MissingMethodException)
    }

    void 'where-queries are the type-checked alternative for anything more complex'() {
        given:
        ['alice', 'alex', 'bob'].each { new User(username: it, passwordHash: 'hash').save(flush: true) }

        when:
        // The closure looks like Groovy code but GORM turns it into a query, not a loop over
        // every row. Spring Data equivalent: a @Query or a Specification.
        def users = User.where { username ==~ 'al%' && username != 'alex' }.list()

        then:
        users*.username == ['alice']
    }

    // ---- Updating and deleting -------------------------------------------------------------

    void 'change a property and save() again to update; delete() removes the row'() {
        given:
        def user = new User(username: 'alice', passwordHash: 'old').save(flush: true)

        when:
        user.passwordHash = 'new'
        user.save(flush: true)

        then:
        User.findByUsername('alice').passwordHash == 'new'

        when:
        user.delete(flush: true)

        then:
        User.count() == 0
    }
}
