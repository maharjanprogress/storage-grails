package com.example

/**
 * Every route of the app, in one place.
 *
 * In Spring Boot each controller method carries its own @GetMapping("/..."). In Grails,
 * controllers have no route annotations at all: this file decides which URL reaches which
 * controller "action" (the Grails name for a request-handling method).
 *
 * It's a Groovy DSL, like the constraints example in Round 1's ClosuresSpec:
 *     get "/api/ping"(controller: 'ping', action: 'index')
 * is a call to get(...) whose argument is a call to a method named "/api/ping" with named arguments.
 */
class UrlMappings {

    static mappings = {
        // The generated app had a catch-all route, "/$controller/$action?/$id?", which made
        // EVERY action of EVERY controller reachable, with ANY HTTP method. That's handy for
        // quick prototypes but risky for an API, so we list each route explicitly instead.

        get "/api/ping"(controller: 'ping', action: 'index')

        // When no route matches (404) or an exception escapes a controller (500), Grails
        // forwards to these actions, so errors come back as JSON instead of HTML pages
        "404"(controller: 'error', action: 'notFound')
        "500"(controller: 'error', action: 'serverError')

        // Browsers request /favicon.ico for pages that declare no icon of their own
        "/favicon.ico"(redirect: [uri: '/assets/favicon.ico', permanent: true])
    }
}
