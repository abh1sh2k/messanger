package com.abhishek.service

import com.abhishek.common.{LoginRequest, Message, Response}
import com.abhishek.data.{RedisClient, UserDataService}
import scredis.Redis

import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  def register(msidn: String)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response]

  def login(request: LoginRequest)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response]

  def listen(userid: String, message: String): Response
}

class UserServiceImpl extends UserService {
  override def register(msidn: String)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response] = {
    service.createUser(msidn).map {
      r =>
        if (r == 1) {
          redisClient.register(msidn)
          Response(success = true)
        }
        else Response(success = false)
    }
  }

  override def login(request: LoginRequest)(service: UserDataService, redisClient: RedisClient)(implicit ex: ExecutionContext): Future[Response] = {
    request.uid match {
      case Some(x) =>
        redisClient.isRegistered(request.msisdn) map {
          registered =>
            if (registered) {
              redisClient.login(request.uid.get)
              Response(success = true)
            } else {
              Response(success = false, message = Some("not registered"))
            }
        }
      case _ => Future.successful(Response(success = false, message = Some("no uuid")))
    }
  }

  override def listen(userid: String, message: String): Response = ???
}
