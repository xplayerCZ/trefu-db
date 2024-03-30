package cz.davidkurzica.domain.routing

import cz.davidkurzica.db.dbQuery
import cz.davidkurzica.domain.connection.Connections
import cz.davidkurzica.domain.connectionrule.ConnectionRules
import cz.davidkurzica.domain.departure.Departures
import cz.davidkurzica.domain.line.Lines
import cz.davidkurzica.domain.packet.PacketService
import cz.davidkurzica.domain.packet.Packets
import cz.davidkurzica.domain.route.Routes
import cz.davidkurzica.domain.routestop.RouteStops
import cz.davidkurzica.domain.rule.RuleService
import cz.davidkurzica.domain.stop.Stop
import cz.davidkurzica.domain.stop.StopService
import cz.davidkurzica.domain.stop.Stops
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.LocalDate
import java.time.LocalTime

class RoutingService(
    private val ruleService: RuleService,
    private val packetService: PacketService,
    private val stopService: StopService,
) {

    suspend fun getStops(
        forDate: LocalDate
    ): List<StopOption> = dbQuery {
        packetService.getPackets(
            offset = null,
            limit = null,
            after = forDate,
            before = forDate,
            valid = true,
        ).singleOrNull()?.let {
            stopService.getStops(null, null, it.id)
        }?.map {
            it.toStopOption()
        } ?: listOf()
    }

    suspend fun getDepartures(
        offset: Int?,
        limit: Int?,
        forDate: LocalDate,
        after: LocalTime,
        stopId: Int,
    ): List<DepartureWithLine> = dbQuery {
        val ruleId = ruleService.getRules(null, null, forDate).singleOrNull()?.id ?: return@dbQuery listOf()

        val query = (Departures innerJoin Connections innerJoin Routes innerJoin RouteStops innerJoin Lines innerJoin Packets innerJoin ConnectionRules)
            .slice(Lines.shortCode, Departures.time, Routes.length, Routes.id)
            .select {
                (Packets.valid eq true)
                    .and (Packets.from lessEq forDate)
                    .and (Packets.to greaterEq forDate)
                    .and (Departures.time greater after)
                    .and (RouteStops.stopId eq stopId)
                    .and (RouteStops.index eq Departures.index)
                    .and (ConnectionRules.ruleId eq ruleId)
            }
            .orderBy(Departures.time)


        query.apply {
            limit?.let { limit(it, (offset ?: 0).toLong()) }
        }

        query.map { it.toDepartureWithLine() }
    }

    suspend fun getTimetables(
        forDate: LocalDate,
        stopId: Int,
        routeId: Int,
        lineShortCode: String,
    ): DepartureTimetable = dbQuery {
        val ruleId = ruleService.getRules(null, null, forDate).singleOrNull()?.id ?: return@dbQuery DepartureTimetable(
            date = forDate,
            departures = listOf()
        )

        val departures = (Departures innerJoin Connections innerJoin Routes innerJoin RouteStops innerJoin Lines innerJoin Packets innerJoin ConnectionRules)
            .slice(Departures.time, Routes.length, Routes.id)
            .select {
                (Packets.valid eq true)
                    .and (Packets.from lessEq forDate)
                    .and (Packets.to greaterEq forDate)
                    .and (RouteStops.stopId eq stopId)
                    .and (Routes.id eq routeId)
                    .and (Lines.shortCode eq lineShortCode)
                    .and (RouteStops.index eq Departures.index)
                    .and (ConnectionRules.ruleId eq ruleId)
            }
            .orderBy(Departures.time)
            .mapNotNull { it.toDepartureSimple() }

        DepartureTimetable(
            date = forDate,
            departures = departures,
        )
    }

    suspend fun getRoutes(
        forDate: LocalDate,
        stopId: Int,
    ): List<RoutingRoute> = dbQuery {
        val result = (RouteStops innerJoin Routes innerJoin Lines innerJoin Packets)
            .slice(Routes.id, Routes.length, Lines.shortCode)
            .select {
                (Packets.valid eq true)
                    .and (Packets.from lessEq forDate)
                    .and (Packets.to greaterEq forDate)
                    .and (RouteStops.stopId eq stopId)
            }

        result.mapNotNull { it.toRoutingRoute() }
    }

    private fun ResultRow.toDepartureWithLine() =
        DepartureWithLine(
            time = this[Departures.time],
            lineShortCode = this[Lines.shortCode],
            stopName = getLastStopName(this[Routes.id], this[Routes.length] - 1)
        )

    private fun ResultRow.toDepartureSimple() = this[Departures.time]?.let {
        DepartureSimple(
            time = it,
            stopName = getLastStopName(this[Routes.id], this[Routes.length] - 1)
        )
    }

    private fun ResultRow.toRoutingRoute() =
        RoutingRoute(
            routeId = this[Routes.id],
            lineShortCode = this[Lines.shortCode],
            lastStopName = getLastStopName(this[Routes.id], this[Routes.length] - 1)
        )

    private fun Stop.toStopOption() =
        StopOption(
            id = id,
            name = name,
            enabled = true // TODO: Implement this
        )

    private fun getLastStopName(routeId: Int, index: Int) =
        (Stops innerJoin RouteStops)
            .slice(Stops.name)
            .select {
                (RouteStops.routeId eq routeId)
                    .and(RouteStops.index eq index)
            }
            .mapNotNull { it[Stops.name] }
            .single()
}
