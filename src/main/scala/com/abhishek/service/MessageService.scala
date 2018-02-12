package com.abhishek.service

import com.abhishek.common.{Message, Response}
import com.abhishek.data.{RedisClient, UserDataService}

import scala.concurrent.{ExecutionContext, Future}

trait MessageService {
  def sendMessage(message: Message)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response]


}

class MessageServiceImpl extends MessageService{

  override def sendMessage(message: Message)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response] = {
    if (message.msg.isEmpty)
      return Future.successful(Response(success = false))
    (message.from, message.to) match {
      case (Some(f) , Some(t)) =>
        val res = for{
          a <- redisClient.isRegistered(Some(f))
          b <- redisClient.isRegistered(Some(t))
          c <- if(a && b)  redisClient.pushMessages(f, t, message.msg) else Future.successful(false)
        } yield {
          Response(success = c)
        }
        res
      case _ => Future.successful(Response(success = false, message = Some("no uuid")))
    }
  }

}
