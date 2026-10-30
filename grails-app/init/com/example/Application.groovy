package com.example

import groovy.transform.CompileStatic

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.util.PlaceholderResolutionException

@CompileStatic
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    // Settings the app can't start without. Their values are ${...} placeholders in application.yml.
    static final List<String> REQUIRED_SETTINGS = ['dataSource.url', 'dataSource.username', 'dataSource.password']

    /**
     * Spring calls this very early, before any database connection is made, because Application
     * implements EnvironmentAware. (Spring Boot equivalent: the same interface on any bean.)
     *
     * Why it's needed: if .env is missing, Grails' own config loader does NOT complain about an
     * unresolved ${DB_USERNAME}. It keeps the literal text, Hibernate then logs in to Postgres as a
     * user named "${DB_USERNAME}", and startup dies with a confusing Hibernate error.
     * Spring's Environment is stricter: getProperty throws when a placeholder has no value, so we
     * read each setting through it and turn that into an error message that says what to do.
     */
    @Override
    void setEnvironment(Environment environment) {
        for (String setting : REQUIRED_SETTINGS) {
            try {
                environment.getProperty(setting)
            } catch (PlaceholderResolutionException e) {
                throw new IllegalStateException(
                    "Can't read '$setting': ${e.message}. " +
                    'Copy .env.example to .env and fill it in, or set it as an environment variable.', e)
            }
        }
    }
}
