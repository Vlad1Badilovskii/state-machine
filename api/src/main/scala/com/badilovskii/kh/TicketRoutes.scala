package com.badilovskii.kh

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.{ ByteString, Timeout }
import com.badilovskii.kh.Command.{ ChangeState, CreateTicket }
import com.badilovskii.kh.Data.Ticket
import com.badilovskii.kh.Endpoints.{ BasicEndpoint, Path, _ }
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds
import tapir.json.circe._
import tapir.server.akkahttp._
import tapir.{ Endpoint, _ }
import EndpointOps._
import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import scala.util.{ Failure, Success }

class TicketRoutes(
    implicit system: ActorSystem,
    executionContext: ExecutionContext,
    timeout: Timeout,
    materializer: ActorMaterializer) extends FailFastCirceSupport with LazyLogging with Endpoints {

    private def ticketEndpoint(base: BasicEndpoint): BasicEndpoint =
        base
            .in("api" / "v1" / "ticket")
            .tag("Ticket")

    private val createTicket = ticketEndpoint(post)
        .description("Create new ticket by providing a name")
        .in(Path.entityId / "create" / Query.name)
        .out(jsonBody[Data])
        .errorOut(stringBody)

    private val changeState = ticketEndpoint(post)
        .description("Change state of existing ticket")
        .in(Path.entityId / "states" / Query.state)
        .out(jsonBody[Data])
        .errorOut(stringBody)

    private val ticketHistory = ticketEndpoint(get)
        .description("Get all history of ticket by entityId")
        .in(Path.entityId / "states")
        .out(jsonBody[Seq[DomainEvent]])
        .errorOut(stringBody)

    private val allHistory = ticketEndpoint(get)
        .description("Get all history of every ticket")
        .in("all-states")
        .out(streamBody[Source[ByteString, Any]](schemaFor[DomainEvent], MediaType.Json()))
        .errorOut(stringBody)

    private val createTicketRoute = createTicket.toRoute {
        case (entityId, name) =>
            val ticketActor = system.actorOf(TicketService.props(entityId))
            val response = (ticketActor ? CreateTicket(name)).mapTo[Data]
            response.map(Right(_))
    }

    private val changeStateRoute = changeState.toRoute {
        case (entityId, state) =>
            val ticketActor = system.actorOf(TicketService.props(entityId))
            (ticketActor ? ChangeState(TicketState.of(state))).transform {
                case Failure(exception) => Failure(exception)
                case Success(value: Data) => Success(Right(value))
                case Success(StateMachineError(message)) => Success(Left(message))
                case e => e.map(a => Left(a.toString))
            }
    }

    private val ticketHistoryRoute = ticketHistory.toRoute { entityId =>
        logger.info(s"Getting all events for ticket: $entityId")
        TicketService.events(entityId)
            .runWith(Sink.seq[DomainEvent])
            .map(Right(_))
    }

    private val allHistoryRoute = allHistory.toRoute { _ =>
        logger.info(s"Getting events for all tickets")
        Future.successful(
            Right(TicketService.events().asJsonArray)
        )
    }

    def route: Route = createTicketRoute ~ changeStateRoute ~ allHistoryRoute ~ ticketHistoryRoute

    def endpoints: Seq[Endpoint[_, _, _, _]] = Seq(createTicket, changeState, ticketHistory, allHistory)
}
