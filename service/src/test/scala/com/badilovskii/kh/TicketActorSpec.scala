package com.badilovskii.kh

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.badilovskii.kh.Command.{ ChangeState, CreateTicket }
import com.badilovskii.kh.Data.Ticket
import com.badilovskii.kh.TicketState.{ Closed, Finished, Init, Pending }
import com.typesafe.config.ConfigFactory
import java.io.File
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._
import scala.reflect.io.Directory

class TicketActorSpec
    extends TestKit(ActorSystem("TicketActorFSMSpec", ConfigFactory.load()))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll {

    override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
    override def beforeAll(): Unit = {
        val directory = new Directory(new File("target/statemachine/journal"))
        directory.deleteRecursively()
    }

    "TicketService" should {
        "create a ticket" in new Scope {
            val entityId = "ut1"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
        }

        "fail to create a ticket: already exists" in new Scope {
            val entityId = "ut2"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! CreateTicket(ticketName)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
        }

        "change state from Init to Pending" in new Scope {
            val entityId = "ut3"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
        }

        "change state from Init to Closed" in new Scope {
            val entityId = "ut4"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Closed)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Closed.identifier))
        }

        "fail to change state from Init: unsupported state transition" in new Scope {
            val entityId = "ut5"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Init)
            ticketActor ! ChangeState(TicketState.Finished)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(StateMachineError("Unsupported state transition from Init to Init"))
            expectMsg(StateMachineError("Unsupported state transition from Init to Finished"))
        }

        "fail to change state from Init: ticket not exist" in new Scope {
            val entityId = "ut6"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! ChangeState(TicketState.Init)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Finished)
            ticketActor ! ChangeState(TicketState.Closed)

            expectMsg(StateMachineError("Unable to change state on Init, ticket not exists"))
            expectMsg(StateMachineError("Unable to change state on Pending, ticket not exists"))
            expectMsg(StateMachineError("Unable to change state on Finished, ticket not exists"))
            expectMsg(StateMachineError("Unable to change state on Closed, ticket not exists"))
        }

        "change state from Pending to Finished" in new Scope {
            val entityId = "ut7"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Finished)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Pending.identifier), Finished.identifier))
        }

        "change state from Pending to Closed" in new Scope {
            val entityId = "ut8"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Closed)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Pending.identifier), Closed.identifier))
        }

        "fail to change state from Pending: unsupported state transition" in new Scope {
            val entityId = "ut9"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Init)
            ticketActor ! ChangeState(TicketState.Pending)

            expectMsg(Ticket(entityId, ticketName, from = None, to = Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
            expectMsg(StateMachineError("Unsupported state transition from Pending to Init"))
            expectMsg(StateMachineError("Unsupported state transition from Pending to Pending"))
        }

        "manually change state from Finished to Closed" in new Scope {
            val entityId = "ut10"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Finished)
            ticketActor ! ChangeState(TicketState.Closed)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Pending.identifier), Finished.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Finished.identifier), Closed.identifier))
        }

        "automatically change state from Finished to Closed" in new Scope {
            val entityId = "ut11"
            val ticketActor = system.actorOf(TicketService.props(entityId))
            implicit val ec = system.dispatcher

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Finished)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Pending.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Pending.identifier), Finished.identifier))

            system.scheduler.scheduleOnce(2.5 seconds, ticketActor, ChangeState(TicketState.Finished))
            expectMsg(StateMachineError("You have reached the final state, no further transition is possible"))
        }

        "stay in Closed state: final state" in new Scope {
            val entityId = "ut12"
            val ticketActor = system.actorOf(TicketService.props(entityId))

            ticketActor ! CreateTicket(ticketName)
            ticketActor ! ChangeState(TicketState.Closed)
            ticketActor ! ChangeState(TicketState.Finished)
            ticketActor ! ChangeState(TicketState.Pending)
            ticketActor ! ChangeState(TicketState.Init)

            expectMsg(Ticket(entityId, ticketName, None, Init.identifier))
            expectMsg(Ticket(entityId, ticketName, Some(Init.identifier), Closed.identifier))
            expectMsg(StateMachineError("You have reached the final state, no further transition is possible"))
            expectMsg(StateMachineError("You have reached the final state, no further transition is possible"))
            expectMsg(StateMachineError("You have reached the final state, no further transition is possible"))
        }
    }
}

trait Scope {
    val entityId: String
    val ticketName = "wi-fi not working"
}
