package cz.davidkurzica.di

import cz.davidkurzica.domain.connection.ConnectionService
import cz.davidkurzica.domain.connectionrule.ConnectionRuleService
import cz.davidkurzica.domain.departure.DepartureService
import cz.davidkurzica.domain.line.LineService
import cz.davidkurzica.domain.packet.PacketService
import cz.davidkurzica.domain.route.RouteService
import cz.davidkurzica.domain.routestop.RouteStopService
import cz.davidkurzica.domain.routing.RoutingService
import cz.davidkurzica.domain.rule.RuleService
import cz.davidkurzica.domain.stop.StopService
import org.koin.dsl.module

val serviceModule = module {
    single { ConnectionService() }
    single { ConnectionRuleService() }
    single { LineService() }
    single { PacketService() }
    single { StopService() }
    single { DepartureService() }
    single { RouteService() }
    single { RouteStopService() }
    single { RuleService() }
    single { RoutingService(get(), get(), get())}
}
