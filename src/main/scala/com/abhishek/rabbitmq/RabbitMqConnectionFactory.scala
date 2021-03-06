package com.abhishek.rabbitmq

import java.io.IOException
import java.util.UUID

import com.abhishek.conf.MqttConfig
import com.rabbitmq.client._;
/**
  * Created by abhishek on 21/02/18
  */
object RabbitMqConnectionFactory {
  private val uuid =  "server1"
  private val EXCHANGE_NAME = "mqtt-exchange"

  private lazy val factory = new ConnectionFactory
  lazy val host = MqttConfig.rabbitHost
  factory.setHost(host)
  val connection = factory.newConnection()

  def getConnection = connection

  def getExchangeName = EXCHANGE_NAME
  def getThisServerID = uuid
}
