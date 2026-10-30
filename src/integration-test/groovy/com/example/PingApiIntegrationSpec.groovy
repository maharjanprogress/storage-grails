package com.example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * An integration test starts the WHOLE app on a random free port and sends real HTTP requests,
 * so the URL mappings, controllers and JSON rendering are all tested together.
 * Spring Boot equivalent: @SpringBootTest(webEnvironment = RANDOM_PORT).
 *
 * Run it with: ./gradlew integrationTest
 * Round 9 covers integration tests properly. This one just checks the Round 2 endpoints.
 */
@Integration
class PingApiIntegrationSpec extends Specification {

    // Spring puts the random port it picked in the "local.server.port" property
    @Value('${local.server.port}')
    Integer serverPort

    HttpClient http = HttpClient.newHttpClient()

    void 'GET /api/ping returns pong'() {
        when:
        def response = send('GET', '/api/ping')

        then:
        response.statusCode() == 200
        json(response).message == 'pong'
    }

    void 'an unknown URL returns a JSON 404'() {
        when:
        def response = send('GET', '/api/does-not-exist')

        then:
        response.statusCode() == 404
        json(response).error == 'Not Found'
        json(response).path == '/api/does-not-exist'
    }

    // Careful if you're used to Spring: a known URL called with the wrong HTTP method gets a 404
    // from Grails, not a 405 "Method Not Allowed", because no mapping matches the method + URL pair
    void 'POST /api/ping is not routed, so it gets a 404'() {
        when:
        def response = send('POST', '/api/ping')

        then:
        response.statusCode() == 404
    }

    private HttpResponse<String> send(String method, String path) {
        def request = HttpRequest.newBuilder(URI.create("http://localhost:$serverPort$path"))
            .method(method, HttpRequest.BodyPublishers.noBody())
            .build()
        http.send(request, HttpResponse.BodyHandlers.ofString())
    }

    // JsonSlurper is Groovy's built-in JSON parser. It turns JSON into maps and lists.
    private static Map json(HttpResponse<String> response) {
        new JsonSlurper().parseText(response.body()) as Map
    }
}
