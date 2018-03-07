package com.abhishek.rabbitmq

import akka.io.Tcp.Message
import com.rabbitmq.client._

/**
  * Created by abhishek on 22/02/18
  */
case class RabbitMqttMessage(topic:String , payload:String)
object RabbitMqttPublisher {

  val connection = RabbitMqConnectionFactory.getConnection
  def publish(topic: String,  message : String) = {
    val channel: Channel = connection.createChannel
    //channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)
    channel.exchangeDeclare(RabbitMqConnectionFactory.getExchangeName, "direct");
    channel.basicPublish(RabbitMqConnectionFactory.getExchangeName, topic, (new AMQP.BasicProperties), message.getBytes)

    println(s"[x] Sent '$message'")

    channel.close()
  }
}