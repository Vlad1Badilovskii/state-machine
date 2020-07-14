package com.badilovskii.kh

import akka.http.scaladsl.model.StatusCodes.{ BadRequest, InternalServerError, NotFound }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse }
import akka.http.scaladsl.server.Directives.{ complete, extractUri }
import akka.http.scaladsl.server.{ ExceptionHandler, RejectionHandler }
import io.circe.syntax._
import io.circe.{ Json, JsonObject }

object CustomExceptionHandler {

  val rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
        .handleNotFound {
          val errorResponse = JsonObject(
            ("code", Json.fromInt(NotFound.intValue)),
            ("type", Json.fromString("NotFound")),
            ("message", Json.fromString("The requested resource could not be found."))
          ).asJson.noSpaces

          complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, errorResponse)))
        }
        .result()

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: IllegalArgumentException =>
      extractUri { _ =>
        complete(HttpResponse(BadRequest, entity = e.getMessage))
      }
    case e: UnsupportedOperationException =>
      extractUri { _ =>
        complete(HttpResponse(BadRequest, entity = e.getMessage))
      }
    case e: RuntimeException =>
      extractUri { _ =>
        println(s"Request to could not be handled normally")
        complete(HttpResponse(InternalServerError, entity = e.getMessage))
      }
  }
}
