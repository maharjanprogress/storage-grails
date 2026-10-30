package com.example

import grails.converters.JSON

/**
 * Turns errors into JSON. UrlMappings.groovy forwards 404s and 500s here.
 *
 * Spring Boot's equivalent is its built-in /error endpoint (BasicErrorController), or an
 * @ControllerAdvice class.
 */
class ErrorController {

    def notFound() {
        response.status = 404
        // request.forwardURI is the URL the client originally asked for,
        // before Grails forwarded the request to this action
        render([error: 'Not Found', path: request.forwardURI] as JSON)
    }

    def serverError() {
        // Grails already writes the exception and its stack trace to the log.
        // We don't send it to the client, since it can reveal internal details.
        response.status = 500
        render([error: 'Internal Server Error'] as JSON)
    }
}
