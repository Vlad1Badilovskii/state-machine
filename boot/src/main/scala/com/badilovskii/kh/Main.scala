package com.badilovskii.kh

import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging{

    logger.info("Starting ticket state machine ...")

    WebServer.startServer("0.0.0.0", 8080)
}
