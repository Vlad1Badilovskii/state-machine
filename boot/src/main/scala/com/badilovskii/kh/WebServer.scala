package com.badilovskii.kh

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContextExecutor }
import scala.util.Try
import CustomExceptionHandler._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import akka.http.scaladsl.model.HttpMethods._


object WebServer extends HttpApp {
    private val serviceName = "state-machine"
    private val timeoutDuration = 30 seconds

    implicit val system = ActorSystem(serviceName, ConfigFactory.load())
    implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
    implicit val mat = ActorMaterializer()
    implicit val timeout = Timeout(timeoutDuration)

    private val ticketEndpoints = new TicketRoutes()
    private val documentationRoutes = new DocumentationRoutes(ticketEndpoints.endpoints)

    private val corsSettings = CorsSettings.defaultSettings
        .withAllowedMethods(List(GET, DELETE, PATCH, PUT, POST, OPTIONS))
        .withAllowGenericHttpRequests(true)

    protected def routes: Route = cors(corsSettings) {
        handleExceptions(exceptionHandler) {
            handleRejections(rejectionHandler) {
                ticketEndpoints.route ~ documentationRoutes.routes
            }
        }
    }

    override def startServer(host: String, port: Int): Unit = super.startServer(host, port, system)

    //graceful shutdown
    override def postServerShutdown(attempt: Try[Done], actorSystem: ActorSystem): Unit = {
        super.postServerShutdown(attempt, actorSystem)

        Await.result(Http().shutdownAllConnectionPools(), timeoutDuration)
        Await.result(system.terminate(), timeoutDuration)
    }
}
