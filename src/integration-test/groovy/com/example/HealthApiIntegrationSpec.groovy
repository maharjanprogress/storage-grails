package com.example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Checks Spring Boot Actuator's health endpoint, including the "db" check we register by hand
 * in resources.groovy. In the "test" environment the database is still in-memory H2 (until
 * Round 9), so this proves the wiring works, while the Postgres check is done by hand.
 */
@Integration
class HealthApiIntegrationSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    void 'GET /actuator/health is UP and includes a working database check'() {
        when:
        def response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("http://localhost:$serverPort/actuator/health")).build(),
            HttpResponse.BodyHandlers.ofString())
        def health = new JsonSlurper().parseText(response.body())

        then:
        response.statusCode() == 200
        health.status == 'UP'
        // Navigating parsed JSON with dots works because JsonSlurper returns nested Maps
        health.components.db.status == 'UP'
        health.components.db.details.database == 'H2'
    }
}
