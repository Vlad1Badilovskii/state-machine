package com.badilovskii.kh

import akka.http.scaladsl.server.Route
import tapir.Endpoint
import tapir.EndpointInput.{ PathCapture, Query }
import tapir._

trait Endpoints {
  def route: Route
  def endpoints: Seq[Endpoint[_, _, _, _]]
}

object Endpoints {
  type BasicEndpoint = Endpoint[Unit, Unit, Unit, Nothing]

  val get: BasicEndpoint = endpoint.get
  val post: BasicEndpoint = endpoint.post
  val delete: BasicEndpoint = endpoint.delete

  object Path {
    val entityId: PathCapture[String] = path[String]("entityId")
  }

  object Query {
    val name: Query[String] = query[String]("name").description("Required parameter for new ticket creation")
    val state: Query[String] = query[String]("state").description("Required parameter for ticket state transition")
  }
}
