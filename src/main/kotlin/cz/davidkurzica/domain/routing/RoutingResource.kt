package cz.davidkurzica.domain.routing

import cz.davidkurzica.util.LocalDateSerializer
import cz.davidkurzica.util.LocalTimeSerializer
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.time.LocalTime


@Serializable
@Resource("/routing/stops")
class RoutingStopsEndpoint(
    @Serializable(with = LocalDateSerializer::class)
    val forDate: LocalDate
)

@Serializable
@Resource("/routing/departures")
class RoutingDeparturesEndpoint(
    val offset: Int? = null,
    val limit: Int? = null,
    @Serializable(with = LocalDateSerializer::class)
    val forDate: LocalDate,
    @Serializable(with = LocalTimeSerializer::class)
    val after: LocalTime,
    val stopId: Int,
)

@Serializable
@Resource("/routing/timetables")
class RoutingTimetablesEndpoint(
    @Serializable(with = LocalDateSerializer::class)
    val forDate: LocalDate,
    val stopId: Int,
    val routeId: Int,
    val lineShortCode: String
)

@Serializable
@Resource("/routing/routes")
class RoutingRoutesEndpoint(
    @Serializable(with = LocalDateSerializer::class)
    val forDate: LocalDate,
    val stopId: Int
)

fun Route.routing() {

    val routingService: RoutingService by inject()

    get<RoutingStopsEndpoint> {
        call.respond(
            routingService.getStops(
                forDate = it.forDate
            )
        )
    }

    get<RoutingDeparturesEndpoint> {
        call.respond(
            routingService.getDepartures(
                offset = it.offset,
                limit = it.limit,
                forDate = it.forDate,
                after = it.after,
                stopId = it.stopId,
            )
        )
    }

    get<RoutingTimetablesEndpoint> {
        call.respond(
            routingService.getTimetables(
                forDate = it.forDate,
                stopId = it.stopId,
                routeId = it.routeId,
                lineShortCode = it.lineShortCode
            )
        )
    }

    get<RoutingRoutesEndpoint> {
        call.respond(
            routingService.getRoutes(
                forDate = it.forDate,
                stopId = it.stopId
            )
        )
    }

}
