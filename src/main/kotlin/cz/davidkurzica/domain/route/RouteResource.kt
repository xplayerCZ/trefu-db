package cz.davidkurzica.domain.route

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
@Resource("/routes")
class RoutesEndpoint(
    val offset: Int? = null,
    val limit: Int? = null,
    val lineId: Int? = null,
    val packetId: Int? = null,
)

@Serializable
@Resource("/routes/{id}")
class RouteByIdEndpoint(val id: Int) {

    @Serializable
    @Resource("/connections")
    class ConnectionsEndpoint(val parent: RouteByIdEndpoint, val offset: Int? = null, val limit: Int? = null)

    @Serializable
    @Resource("/routes")
    class RoutesEndpoint(val parent: RouteByIdEndpoint, val offset: Int? = null, val limit: Int? = null)
}

fun io.ktor.server.routing.Route.route() {

    val routeService: RouteService by inject()

    get<RoutesEndpoint> {
        call.respond(
            routeService.getRoutes(
                offset = it.offset,
                limit = it.limit,
                lineId = it.lineId,
                packetId = it.packetId,
            )
        )
    }

    post<RoutesEndpoint> {
        try {
            val route = call.receive<NewRoute>()
            call.respond(
                message = routeService.addRoute(route),
                status = HttpStatusCode.Created
            )
        } catch (e: ContentTransformationException) {
            call.respondText("Route is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    get<RouteByIdEndpoint> {
        val route =
            routeService.getRouteById(it.id) ?: return@get call.respondText(
                "No route with id ${it.id}",
                status = HttpStatusCode.NotFound
            )
        call.respond(route)
    }

    put<RouteByIdEndpoint> {
        try {
            val route = call.receive<NewRoute>()
            routeService.editRoute(route, it.id)
            call.respondText("Route with id ${it.id} updated correctly", status = HttpStatusCode.OK)
        } catch (e: ContentTransformationException) {
            call.respondText("Route is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    delete<RouteByIdEndpoint> {
        if (routeService.deleteRouteById(it.id)) {
            call.respondText("Route with id ${it.id} deleted successfully", status = HttpStatusCode.OK)
        } else {
            call.respondText("Failed to delete Route with id ${it.id}", status = HttpStatusCode.InternalServerError)
        }
    }

    get<RouteByIdEndpoint.ConnectionsEndpoint> {
        call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
    }

    get<RouteByIdEndpoint.RoutesEndpoint> {
        call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
    }
}
