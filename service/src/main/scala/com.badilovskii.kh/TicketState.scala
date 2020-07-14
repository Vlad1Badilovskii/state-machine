package com.badilovskii.kh

import akka.persistence.fsm.PersistentFSM.FSMState

sealed trait TicketState extends FSMState

object TicketState {
    def of(state: String): TicketState = state match {
        case Init.identifier => Init
        case Pending.identifier => Pending
        case Finished.identifier => Finished
        case Closed.identifier => Closed
    }

    case object Init extends TicketState {
        override val identifier: String = "Init"
    }

    case object Pending extends TicketState {
        override val identifier: String = "Pending"
    }

    case object Finished extends TicketState {
        override val identifier: String = "Finished"
    }

    case object Closed extends TicketState {
        override val identifier: String = "Closed"
    }
}
