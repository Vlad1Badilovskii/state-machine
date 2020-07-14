package com.badilovskii.kh

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.server.directives.ExecutionDirectives
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.util.Timeout
import com.badilovskii.kh.CustomExceptionHandler._
import com.badilovskii.kh.Data.Ticket
import com.typesafe.config.ConfigFactory
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.syntax._
import java.io.File
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.concurrent.Eventually._
import scala.concurrent.duration._
import scala.reflect.io.Directory

class TicketRoutesSpec
    extends AnyWordSpecLike
    with IdiomaticMockito
    with ScalatestRouteTest
    with ExecutionDirectives
    with BeforeAndAfterAll
    with Matchers {

    implicit val timeout: Timeout = Timeout(5 seconds)
    implicit val default: RouteTestTimeout = RouteTestTimeout(5 seconds)
    def responseAsJson: Json = parse(responseAs[String]).getOrElse(Json.fromString(""))

    override def createActorSystem(): ActorSystem = ActorSystem("TicketRoutesSpec", ConfigFactory.load())

    override def beforeAll(): Unit = {
        val directory = new Directory(new File("target/statemachine/journal"))
        directory.deleteRecursively()
    }

    private val route = handleExceptions(exceptionHandler){
        handleRejections(rejectionHandler) {
            new TicketRoutes().route
        }
    }

    "TicketRoutes" should {
        "create new ticket by providing a name" in {
            Post("/api/v1/ticket/it1/create?name=wi-fi-is-broken") ~> route ~> check {
                contentType shouldBe ContentTypes.`application/json`
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it1", "wi-fi-is-broken", None, "Init")
                responseAsJson shouldBe data.asJson
            }
        }

        "change state from Init to Pending" in {
            Post("/api/v1/ticket/it2/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it2/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it2", "wi-fi-is-broken", Some("Init"), "Pending")
                responseAsJson shouldBe data.asJson
            }
        }

        "change state from Init to Closed" in {
            Post("/api/v1/ticket/it3/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it3/states?state=Closed") ~> route ~>  check {
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it3", "wi-fi-is-broken", Some("Init"), "Closed")
                responseAsJson shouldBe data.asJson
            }
        }

        "fail to change state from Init: unsupported state transition" in {
            Post("/api/v1/ticket/it4/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it4/states?state=Finished") ~> route ~>  check {
                status shouldBe StatusCodes.BadRequest
                responseAs[String] shouldBe "Unsupported state transition from Init to Finished"
            }
        }

        "fail to change state from Init: ticket not exist" in {
            Post("/api/v1/ticket/it5/states?state=Finished") ~> route ~>  check {
                status shouldBe StatusCodes.BadRequest
                responseAs[String] shouldBe "Unable to change state on Finished, ticket not exists"
            }
        }

        "change state from Pending to Finished" in {
            Post("/api/v1/ticket/it6/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it6/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it6/states?state=Finished") ~> route ~>  check {
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it6", "wi-fi-is-broken", Some("Pending"), "Finished")
                responseAsJson shouldBe data.asJson
            }
        }

        "change state from Pending to Closed" in {
            Post("/api/v1/ticket/it7/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it7/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it7/states?state=Closed") ~> route ~>  check {
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it7", "wi-fi-is-broken", Some("Pending"), "Closed")
                responseAsJson shouldBe data.asJson
            }
        }

        "fail to change state from Pending: unsupported state transition" in {
            Post("/api/v1/ticket/it8/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it8/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it8/states?state=Init") ~> route ~>  check {
                status shouldBe StatusCodes.BadRequest
                responseAs[String] shouldBe "Unsupported state transition from Pending to Init"
            }
            Post("/api/v1/ticket/it8/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.BadRequest
                responseAs[String] shouldBe "Unsupported state transition from Pending to Pending"
            }
        }

        "change state from Finished to Closed" in {
            Post("/api/v1/ticket/it9/create?name=wi-fi-is-broken") ~> route ~> check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it9/states?state=Pending") ~> route ~>  check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it9/states?state=Finished") ~> route ~>  check {
                status shouldBe StatusCodes.OK
            }
            Post("/api/v1/ticket/it9/states?state=Closed") ~> route ~>  check {
                status shouldBe StatusCodes.OK
                val data: Data = Ticket("it9", "wi-fi-is-broken", Some("Finished"), "Closed")
                responseAsJson shouldBe data.asJson
            }
        }
    }
}
