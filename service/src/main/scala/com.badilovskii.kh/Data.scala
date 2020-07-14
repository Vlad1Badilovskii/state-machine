package com.badilovskii.kh

sealed trait Data

object Data {
    case object Empty extends Data
    case class Ticket(entityId: String, name: String, from: Option[String], to: String) extends Data
}
