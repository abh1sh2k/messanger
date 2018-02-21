package com.abhishek.main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.abhishek.http.HttpServer
import com.abhishek.mqtt.TcpServer
import com.abhishek.route.UserRoute

import scala.io.StdIn

object Boot extends App{

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    //HttpServer.startHttpService
    TcpServer.startServer

}
