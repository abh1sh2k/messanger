package com.abhishek.rabbitmq

import java.io.IOException
import java.util.UUID

import com.rabbitmq.client._;
/**
  * Created by abhishek on 21/02/18
  */
object RabbitMqConnectionFactory {
  private val uuid =  UUID.randomUUID().toString()
  private val EXCHANGE_NAME = "mqtt-exchange"

  private lazy val factory = new ConnectionFactory
  def getConnection = factory.newConnection
  def getExchangeName = EXCHANGE_NAME
  def getThisServerID = uuid
}
