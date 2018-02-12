package com.abhishek.data

import scredis.Redis

import scala.concurrent.Future
import scala.util.{Failure, Try}

trait RedisClient{
  def login(uid : String)
  def register(msisdn : String)
  def isRegistered(msisdn : Option[String]): Future[Boolean]
  def pushMessages(from: String, to : String, messages : List[String] ): Future[Boolean]
}
object RedisClient {
  private lazy val cl = new Redis
  def getRedis() = cl
}
class RedisClientImpl extends RedisClient {
  val client = RedisClient.getRedis()
  val logged = "LOGGED"
  val registered = "REGISTERED"

  def login(uid : String) = {
    client.hSet(logged, uid , true)
  }
  def register(msisdn : String) = {
    client.hSet(registered, msisdn , true)
  }

  def isRegistered(msisdn : Option[String]): Future[Boolean] = {
      msisdn match {
        case Some(x) => client.hExists(registered, x)
        case _ => Future.successful(false)
      }
  }
  def pushMessages(from: String, to : String, messages : List[String] ): Future[Boolean] ={
    client.hSet(from,to,messages.toString())
  }

}
