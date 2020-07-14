package com.badilovskii.kh

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.circe.Encoder
import io.circe.syntax._

object EndpointOps {
  implicit class ArrayJsonConverter[T: Encoder](source: Source[T, NotUsed]) {
    def asJsonArray: Source[ByteString, NotUsed] = {
      source
        .map(v => ByteString(v.asJson.noSpaces))
        .intersperse(ByteString("["), ByteString(", "), ByteString("]"))
    }
  }
}
