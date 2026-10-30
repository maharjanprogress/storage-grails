package com.example

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * A controller unit test: no server, no URL mappings, just the controller class.
 * Spring Boot's closest equivalent is @WebMvcTest.
 *
 * The ControllerUnitTest trait (traits are covered in Round 1's ClassesSpec) adds ready-made
 * properties to this test: "controller" (an instance of PingController), plus a fake
 * "request" and "response".
 */
class PingControllerSpec extends Specification implements ControllerUnitTest<PingController> {

    void 'index answers with pong as JSON'() {
        when:
        controller.index()

        then:
        response.status == 200
        response.contentType.startsWith('application/json')
        response.json.message == 'pong'       // response.json parses the body for you
    }

    void 'index reports the Grails environment, which is always "test" while tests run'() {
        when:
        controller.index()

        then:
        response.json.environment == 'test'
    }
}
