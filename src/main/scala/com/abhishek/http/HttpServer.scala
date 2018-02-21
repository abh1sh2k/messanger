package com.abhishek.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.abhishek.route.UserRoute

import scala.concurrent.ExecutionContext
import scala.io.StdIn

/**
  * Created by abhishek on 20/02/18
  */
object HttpServer {
  // needed to run the route
  def startHttpService(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext)= {
    val route = new UserRoute().route

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture.flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
