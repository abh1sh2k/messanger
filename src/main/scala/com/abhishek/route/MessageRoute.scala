package com.abhishek.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, post}
import akka.http.scaladsl.server.Route
import com.abhishek.common.{JsonSupport, Message}
import com.abhishek.data.{RedisClientImpl, UserDataServiceImpl}
import com.abhishek.service.MessageServiceImpl

import scala.concurrent.{ExecutionContextExecutor, Future}

class MessageRoute extends JsonSupport{
  lazy val userDataService = new UserDataServiceImpl
  lazy val service = new MessageServiceImpl
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val redisClient = new RedisClientImpl

  val route: Route = post {
    path("sendMessage") {
      entity(as[Message]) { message =>
        val response = Future {
          calculateTime{
            service.sendMessage(message)(userDataService, redisClient)
          }
        }
        complete(response)
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
