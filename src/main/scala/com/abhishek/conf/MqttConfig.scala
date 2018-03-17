package com.abhishek.conf

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by abhishek on 14/03/18
  */
object MqttConfig {
  def start = {}
  lazy val config = ConfigFactory.load()
  val rabbitConfig = config.getConfig("rabbitmq")
  val mysqlConfig = config.getConfig("mysql")
  val redisConfig = config.getConfig("redis")
  val rabbitHost = rabbitConfig.getString("host")
  val mysqlHost = mysqlConfig.getString("host")
}
