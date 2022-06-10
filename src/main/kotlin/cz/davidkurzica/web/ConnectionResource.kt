package cz.davidkurzica.web

import cz.davidkurzica.service.ConnectionService
import cz.davidkurzica.model.NewConnection
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlinx.serialization.*

@Serializable
@Resource("/connections")
class Connections(val offset: Int? = 0, val limit: Int? = 20)

@Serializable
@Resource("/connections/{id}")
class ConnectionById(val id: Int) {

    @Serializable
    @Resource("/departures")
    class Departures(val parent: ConnectionById, val offset: Int? = 0, val limit: Int? = 20)

    @Serializable
    @Resource("/rules")
    class Rules(val parent: ConnectionById, val offset: Int? = 0, val limit: Int? = 20)
}

fun Route.connection() {

    val connectionService: ConnectionService by inject()

    get<Connections> {
        call.respond(
            connectionService.getConnections(
                offset = it.offset,
                limit = it.limit
            )
        )
    }

    post<Connections> {
        try {
            val connection = call.receive<NewConnection>()
            connectionService.addConnection(connection)
            call.respondText("Connection stored correctly", status = HttpStatusCode.Created)
        } catch (e: ContentTransformationException) {
            call.respondText("Connection is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    get<ConnectionById> {
        val connection =
            connectionService.getConnectionById(it.id) ?: return@get call.respondText(
                "No connection with id ${it.id}",
                status = HttpStatusCode.NotFound
            )
        call.respond(connection)
    }

    put<ConnectionById> {
        try {
            val connection = call.receive<NewConnection>()
            connectionService.editConnection(connection, it.id)
            call.respondText("Connection with id ${it.id} updated correctly", status = HttpStatusCode.OK)
        } catch (e: ContentTransformationException) {
            call.respondText("Connection is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    get<ConnectionById.Departures> {
        call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
    }

    get<ConnectionById.Rules> {
        call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
    }
}
