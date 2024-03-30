package cz.davidkurzica.plugins
import cz.davidkurzica.domain.connection.connection
import cz.davidkurzica.domain.connectionrule.connectionRule
import cz.davidkurzica.domain.departure.departure
import cz.davidkurzica.domain.line.line
import cz.davidkurzica.domain.packet.packet
import cz.davidkurzica.domain.route.route
import cz.davidkurzica.domain.routestop.routeStop
import cz.davidkurzica.domain.routing.routing
import cz.davidkurzica.domain.rule.rule
import cz.davidkurzica.domain.stop.stop
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        connection()
        connectionRule()
        line()
        packet()
        stop()
        departure()
        route()
        routeStop()
        rule()
        routing()
    }
}