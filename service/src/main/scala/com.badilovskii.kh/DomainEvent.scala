package com.badilovskii.kh

import com.badilovskii.kh.Data.Ticket

sealed trait DomainEvent

object DomainEvent {

    case class TicketCreated(name: String) extends DomainEvent

    case class TicketProcessed(ticket: Ticket) extends DomainEvent

    case class TicketFinished(ticket: Ticket) extends DomainEvent

    case class TicketClosed(ticket: Ticket) extends DomainEvent

    case class StateTransitionRejected(reason: String) extends DomainEvent
}
