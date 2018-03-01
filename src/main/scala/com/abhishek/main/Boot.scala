package com.abhishek.main

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.abhishek.http.HttpServer
import com.abhishek.mqtt.MqttServer
import com.abhishek.rabbitmq.{RabbitMqConnectionFactory, RabbitMqttConsumer, RabbitMqttPublisher}

import scala.io.StdIn

object Boot extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  //HttpServer.startHttpService
  MqttServer.startServer
  //RabbitMqttConsumer.startConsume
  RabbitMqttConsumer.init
  RabbitMqttPublisher.publish(RabbitMqConnectionFactory.getThisServerID, "Hello1")
  Thread.sleep(2000)
  RabbitMqttPublisher.publish(RabbitMqConnectionFactory.getThisServerID, "Hello2")
}
