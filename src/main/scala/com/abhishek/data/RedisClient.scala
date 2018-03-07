package com.abhishek.data

import com.abhishek.rabbitmq.RabbitMqConnectionFactory
import scredis.Redis

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

trait RedisClient{
  def login(uid : String)
  def register(msisdn : String)
  def isRegistered(msisdn : Option[String]): Future[Boolean]
  def pushMessages(from: String, to : String, messages : List[String] ): Future[Boolean]
}
object RedisClient {
  private lazy val redis = new Redis
  private lazy val rediscl = new RedisClientImpl
  def getRedis() = redis
  def getRedisClient() = rediscl
}
class RedisClientImpl extends RedisClient {
  import com.abhishek.main.MqttExecutionContexts.mqttExecutionContext
  val client = RedisClient.getRedis()
  val logged = "LOGGED"
  val registered = "REGISTERED"
  val mqttConnectionMap = "MQTT-CONNECTION"

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
  def registerClientOnServer(machine : String, clientId : String) = {
    val machine = RabbitMqConnectionFactory.getThisServerID
    client.hmSet(mqttConnectionMap, Map(clientId -> machine))
  }
  def registerClientOnThisServer(clientId : String ) = {
    val machine = RabbitMqConnectionFactory.getThisServerID
    registerClientOnServer(machine, clientId)
  }

  def getClientServer(clientId : String) = client.hGet(mqttConnectionMap, clientId)

  def unregisterClient(clientId: String) = client.hDel(mqttConnectionMap, clientId)


}
