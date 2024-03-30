package cz.davidkurzica.domain.routing

import cz.davidkurzica.util.LocalDateSerializer
import cz.davidkurzica.util.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class DepartureWithLine(
    val time: @Serializable(with = LocalTimeSerializer::class) LocalTime?,
    val lineShortCode: String,
    val stopName: String,
)

@Serializable
data class DepartureSimple(
    val time: @Serializable(with = LocalTimeSerializer::class) LocalTime,
    val stopName: String,
)

@Serializable
data class DepartureTimetable(
    val date: @Serializable(with = LocalDateSerializer::class) LocalDate,
    val departures: List<DepartureSimple>,
)

@Serializable
data class RoutingRoute(
    val routeId: Int,
    val lineShortCode: String,
    val lastStopName: String,
)

@Serializable
data class StopOption(
    val id: Int,
    val name: String,
    val enabled: Boolean,
)
