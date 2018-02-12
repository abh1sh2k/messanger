package com.abhishek.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.abhishek.common.{JsonSupport, LoginRequest, RegisterRequest, Response}
import com.abhishek.data.{RedisClientImpl, UserDataServiceImpl}
import com.abhishek.service.UserServiceImpl

import scala.concurrent.{ExecutionContextExecutor, Future}

class UserRoute extends JsonSupport{

  lazy val userDataService = new UserDataServiceImpl
  lazy val service = new UserServiceImpl
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val redisClient = new RedisClientImpl

  val route: Route = post {
      path("register") {
        entity(as[RegisterRequest]) { request =>
          val response = Future {
            calculateTime{
              service.register(request.msisdn.getOrElse(""))(userDataService, redisClient)
            }
          }
          complete(response)
        }
      }
  } ~
    path("login") {
      entity(as[LoginRequest]) { request =>
        val response = Future {
          calculateTime{
            service.login(request)(userDataService, redisClient)
          }
        }
        complete(response)
      }
    } ~
  get {
    path("health") {
      calculateTime{
        complete("ok\n")
      }
    }
  }

  def calculateTime[T](method : => T) : T = {
    val startTime = System.nanoTime
    val r = method
    val endTime = System.nanoTime
    val duration = endTime - startTime
    println("duration " , duration)
    r
  }
}
