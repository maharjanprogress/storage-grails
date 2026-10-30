package com.example

import java.time.Instant

/**
 * A domain class: anything in grails-app/domain is a database entity, no annotation needed.
 * Spring Boot equivalent: a JPA @Entity PLUS its JpaRepository, rolled into one class.
 *
 * GORM (Grails' ORM, running on Hibernate here) adds a lot to this class at compile time, so
 * you get all of these without writing them:
 *   - an "id" (Long, auto-generated) and a "version" (for optimistic locking, like @Version)
 *   - instance methods: save(), delete(), validate(), and an "errors" property
 *   - static methods: User.get(id), User.list(), User.count(), and "dynamic finders" such as
 *     User.findByUsername('alice'), made up from the property names (like Spring Data's
 *     derived query methods, but with no repository interface to declare)
 */
class User {

    String username
    // Only ever a BCrypt hash (Round 5), never the password itself
    String passwordHash

    // GORM's "auto timestamping": properties with exactly these names are filled in for you
    // on insert / on every update. Spring Data equivalent: @CreatedDate / @LastModifiedDate.
    Instant dateCreated
    Instant lastUpdated

    /**
     * Validation rules (like Bean Validation's @NotBlank, @Size, @Pattern).
     * This closure is the DSL from Round 1's ClosuresSpec: "username blank: false, ..." is a call
     * to a method named username(Map) that doesn't exist, which GORM catches and records as rules.
     *
     * Grails 8 change: a property is NULLABLE unless it says nullable: false. (Grails 7 and older
     * did the opposite, and most tutorials online still say so.) That now matches JPA and Bean
     * Validation, where a field is optional until you add @Column(nullable = false) / @NotNull.
     * blank: false alone does NOT reject null: it only checks strings that exist.
     */
    static constraints = {
        // Lowercase letters, digits and "_" only. That avoids "Alice" and "alice" being two
        // different accounts; registration (Round 5) lowercases what the user typed.
        username nullable: false, blank: false, size: 3..30, matches: /[a-z0-9_]+/, unique: true
        passwordHash nullable: false, blank: false
    }

    /**
     * How the class maps onto tables and columns (like JPA's @Table / @Column).
     * Without it the table would be called "user", which is a reserved word in Postgres (and in
     * H2): "create table user (...)" is a syntax error.
     * Columns get snake_case names on their own: passwordHash → password_hash.
     */
    static mapping = {
        table 'users'
    }
}
