package com.example

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

/**
 * Tests the routes in UrlMappings.groovy without starting a server.
 * Spring Boot has no direct equivalent, because there the routes live on the controllers.
 */
class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    void setup() {
        // The mapping test only knows about controllers we register here
        mockController(PingController)
        mockController(ErrorController)
    }

    // verifyForwardUrlMapping(url, controller: ..., action: ...) asks: "does this URL reach
    // this controller and action?" The named arguments become a Map, as in Round 1's ClosuresSpec.
    void 'GET /api/ping is routed to PingController.index'() {
        expect:
        verifyForwardUrlMapping('/api/ping', controller: 'ping', action: 'index')
    }

    void 'POST /api/ping is not routed, because the mapping starts with "get"'() {
        when:
        request.method = 'POST'

        then:
        !verifyForwardUrlMapping('/api/ping', controller: 'ping', action: 'index')
    }

    void 'the old catch-all route is gone, so /ping/index does not reach the controller'() {
        expect:
        !verifyForwardUrlMapping('/ping/index', controller: 'ping', action: 'index')
    }

    void 'a 404 is routed to ErrorController.notFound'() {
        expect:
        verifyForwardUrlMapping(404, controller: 'error', action: 'notFound')
    }

    void 'a 500 is routed to ErrorController.serverError'() {
        expect:
        verifyForwardUrlMapping(500, controller: 'error', action: 'serverError')
    }
}
