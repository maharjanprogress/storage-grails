package com.example

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ErrorControllerSpec extends Specification implements ControllerUnitTest<ErrorController> {

    void 'notFound answers 404 with a JSON error'() {
        when:
        controller.notFound()

        then:
        response.status == 404
        response.json.error == 'Not Found'
    }

    void 'serverError answers 500 without leaking exception details'() {
        when:
        controller.serverError()

        then:
        response.status == 500
        response.json.error == 'Internal Server Error'
        response.json.keySet() == ['error'] as Set
    }
}
