package com.badilovskii.kh

sealed trait Command

object Command {
    case class ChangeState(to: TicketState) extends Command
    case class CreateTicket(name: String) extends Command
}
