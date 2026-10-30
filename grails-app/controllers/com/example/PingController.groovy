package com.example

import grails.converters.JSON
import grails.util.Environment

/**
 * A tiny endpoint for checking the app is up: GET /api/ping
 *
 * There's no @RestController here. Grails treats this class as a controller because it lives in
 * grails-app/controllers/ and its name ends in "Controller". The route comes from UrlMappings.groovy.
 */
class PingController {

    // An "action". Grails calls it when a request is routed to controller 'ping', action 'index'.
    // "def" as a return type means "returns anything": actions usually call render() instead of
    // returning a value.
    def index() {
        // "as JSON" converts the map into a JSON response, like returning a Map from a
        // Spring @RestController method and letting Jackson serialise it.
        render([
            message    : 'pong',
            environment: Environment.current.name   // development, test or production
        ] as JSON)
    }
}
