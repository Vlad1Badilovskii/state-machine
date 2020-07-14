package com.badilovskii.kh

import akka.NotUsed
import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ ActorLogging, ActorSystem, AllForOneStrategy, Props, SupervisorStrategy }
import akka.event.Logging
import akka.persistence.fsm.PersistentFSM
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{ Offset, PersistenceQuery }
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, Attributes }
import com.badilovskii.kh.Command._
import com.badilovskii.kh.Data._
import com.badilovskii.kh.DomainEvent._
import com.badilovskii.kh.TicketState._
import scala.concurrent.duration._
import scala.reflect.ClassTag

class TicketService(entityId: String)
    extends PersistentFSM[TicketState, Data, DomainEvent]
    with ActorLogging {

    startWith(Init, Empty)

    when(Init) {
        case Event(CreateTicket(name), Empty) =>
            stay() applying TicketCreated(name) andThen(_ => sender() ! stateData)
        case Event(CreateTicket(_), Ticket(id, _, _, _)) =>
            stay() applying StateTransitionRejected(s"Ticket with id: $id already created.") replying stateData
        case Event(ChangeState(to), ticket: Ticket) =>
            to match {
                case Pending => goto(Pending) applying TicketProcessed(ticket.copy(from = Some(ticket.to), to = to.identifier)) andThen(_ => sender() ! stateData)
                case Closed => goto(Closed) applying TicketClosed(ticket.copy(from = Some(ticket.to), to = to.identifier)) andThen(_ => sender() ! stateData)
                case _ =>
                    val message = s"Unsupported state transition from Init to ${to.identifier}"
                    stay() applying StateTransitionRejected(message) replying StateMachineError(message)
            }
        case Event(ChangeState(to), Empty) =>
            val message = s"Unable to change state on ${to.identifier}, ticket not exists"
            stay() applying StateTransitionRejected(message) replying StateMachineError(message)
    }

    when(Pending){
        case Event(ChangeState(to), ticket: Ticket) =>
            to match {
                case Finished => goto(Finished) applying TicketFinished(ticket.copy(from = Some(ticket.to), to = to.identifier)) andThen(_ => sender() ! stateData)
                case Closed => goto(Closed) applying TicketClosed(ticket.copy(from = Some(ticket.to), to = to.identifier)) andThen(_ => sender() ! stateData)
                case _ =>
                    val message = s"Unsupported state transition from Pending to ${to.identifier}"
                    stay() applying StateTransitionRejected(message) replying StateMachineError(message)
            }
    }

    when(Finished, stateTimeout = 2 seconds) {
        case Event(StateTimeout, ticket: Ticket) =>
            log.info("Finished state timeout, automatic transition to Closed state ")
            goto(Closed) applying TicketClosed(ticket.copy(from = Some(ticket.to), to = Closed.identifier))
        case Event(ChangeState(to), ticket: Ticket) =>
            to match {
                case Closed => goto(Closed) applying TicketClosed(ticket.copy(from = Some(ticket.to), to = to.identifier)) andThen(_ => sender() ! stateData)
                case _ =>
                    val message = s"Unsupported state transition from Finished to ${to.identifier}"
                    stay() applying StateTransitionRejected(message) replying StateMachineError(message)
            }
    }

    when(Closed) {
        case _ =>
            val message = s"You have reached the final state, no further transition is possible"
            stay() applying StateTransitionRejected(message) replying StateMachineError(message)
    }

    whenUnhandled {
        case Event(_, _) =>
            val message = "Unsupported state transition"
            stay() applying StateTransitionRejected(message) replying StateMachineError(message)
    }

    onTransition {
        case stateA -> stateB => log.info(s"Transitioning from $stateA to $stateB")
    }

    override val supervisorStrategy: SupervisorStrategy =
        AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
            case _ => Resume
        }

    override def persistenceId: String = entityId

    override def domainEventClassTag: ClassTag[DomainEvent] = ClassTag(classOf[DomainEvent])

    override def applyEvent(domainEvent: DomainEvent, currentData: Data): Data = {
        domainEvent match {
            case TicketCreated(name) => Ticket(entityId, name, None, Init.identifier)
            case TicketProcessed(ticket) => ticket
            case TicketFinished(ticket) => ticket
            case TicketClosed(ticket) => ticket
            case StateTransitionRejected(reason) =>
                log.error(s"State transition rejected: $reason")
                currentData
        }
    }
}

object TicketService {
    def props(entityId: String): Props = Props(new TicketService(entityId))

    private val journal: ActorSystem => LeveldbReadJournal = s =>  {
        PersistenceQuery(s).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    }

    def events(entityId: String)(implicit system: ActorSystem, mat: ActorMaterializer): Source[DomainEvent, NotUsed] = {
        journal(system)
            .eventsByTag("state", Offset.noOffset)
            .filter(_.persistenceId == entityId)
            .map(_.event)
            .collectType[DomainEvent]
            .log(s"$entityId:eventsStream")
            .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
    }


    def events()(implicit system: ActorSystem, mat: ActorMaterializer): Source[DomainEvent, NotUsed] = {
        journal(system)
            .eventsByTag("state", Offset.noOffset)
            .map(_.event)
            .collectType[DomainEvent]
            .log("all:eventsStream")
            .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
    }
}
