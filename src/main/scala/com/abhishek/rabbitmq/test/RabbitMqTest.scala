package com.abhishek.rabbitmq.test

import com.rabbitmq.client._

object RabbitmqQueue {

  def main(args: Array[String]): Unit = {
    val factory = new ConnectionFactory()

    val connection = factory.newConnection()
    val queue = "task hello"

    val channel: Channel = connection.createChannel()

    channel.queueDeclare("hello",false,false,false,null)
    channel.queueDeclare()

    channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, "hello world ok".getBytes)
    channel.close

    Thread.sleep(2000)

    val channel1: Channel = connection.createChannel()

    import com.rabbitmq.client.AMQP
    import java.io.IOException
    channel1.queueDeclare(queue, false, false, false, null)

    val consumer = new DefaultConsumer(channel1) {


      @throws[IOException]
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        System.out.println(" [x] Received '" + message + "'")
      }
    };

    channel1.basicConsume(queue, true, consumer)
    channel1.close()
    connection.close()
    def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")

  }


}
