package cz.davidkurzica.web

import cz.davidkurzica.service.LineService
import cz.davidkurzica.model.NewLine
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
@Resource("/lines")
class Lines(val offset: Int? = 0, val limit: Int? = 20)

@Serializable
@Resource("/lines/{id}")
class LineById(val id: Int) {

    @Serializable
    @Resource("/routes")
    class Routes(val parent: LineById, val offset: Int? = 0, val limit: Int? = 20)
}

fun Route.line() {

    val lineService: LineService by inject()

    get<Lines> {
        call.respond(
            lineService.getLines(
                offset = it.offset,
                limit = it.limit
            )
        )
    }

    post<Lines> {
        try {
            val line = call.receive<NewLine>()
            lineService.addLine(line)
            call.respondText("Line stored correctly", status = HttpStatusCode.Created)
        } catch (e: ContentTransformationException) {
            call.respondText("Line is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    get<LineById> {
        val line =
            lineService.getLineById(it.id) ?: return@get call.respondText(
                "No line with id ${it.id}",
                status = HttpStatusCode.NotFound
            )
        call.respond(line)
    }

    put<LineById> {
        try {
            val line = call.receive<NewLine>()
            lineService.editLine(line, it.id)
            call.respondText("Line with id ${it.id} updated correctly", status = HttpStatusCode.OK)
        } catch (e: ContentTransformationException) {
            call.respondText("Line is in wrong format", status = HttpStatusCode.BadRequest)
        }
    }

    get<LineById.Routes> {
        call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
    }
}
