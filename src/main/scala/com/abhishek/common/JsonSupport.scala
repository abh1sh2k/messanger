package com.abhishek.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat2(Response)
  implicit val logingFormat = jsonFormat2(LoginRequest)
  implicit val registerFormat = jsonFormat1(RegisterRequest)
  implicit val messageFormat = jsonFormat3(Message)
}
