package com.badilovskii.kh

import akka.http.scaladsl.server.Route
import tapir.Endpoint
import tapir.swagger.akkahttp._
import tapir.openapi.circe.yaml._
import tapir.docs.openapi._

class DocumentationRoutes(endpoints: Seq[Endpoint[_, _, _, _]]) {
  def routes: Route = {
    val openApiDocs = endpoints.toOpenAPI("Ticket State Machine API", "1.0")
    new SwaggerAkka(openApiDocs.toYaml).routes
  }
}
