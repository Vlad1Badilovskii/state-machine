package com.badilovskii.kh

import akka.persistence.journal.{ Tagged, WriteEventAdapter }
import com.badilovskii.kh.DomainEvent.{ StateTransitionRejected, TicketCreated }

class TicketEventAdapter extends WriteEventAdapter {
    def manifest(event: Any): String = "stateMachine"

    def toJournal(event: Any): Any = event match {
        case e: StateTransitionRejected => e
        case e => Tagged(e, Set("state"))
    }
}
