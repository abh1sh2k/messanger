package com.abhishek.rabbitmq

import java.io.IOException
import java.util.TimerTask

import com.rabbitmq.client._

/**
  * Created by abhishek on 22/02/18
  */
object RabbitMqttConsumer  {
    def init (conn: Connection)= {
      val connection = conn
      val channel: Channel = connection.createChannel()
      val exchangeName = RabbitMqConnectionFactory.getExchangeName
      channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT)
      val queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, exchangeName, RabbitMqConnectionFactory.getThisServerID)
      channel.basicConsume(queueName, true, new RabbitMqttConsumer(channel))
    }
}
class RabbitMqttConsumer(channel:Channel) extends DefaultConsumer(channel:Channel){
  @Override override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
    val message: String = new String(body, "UTF-8")
    println(s"[x] Received '$message'")
  }
}