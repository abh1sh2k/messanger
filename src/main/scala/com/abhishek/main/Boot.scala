package com.abhishek.main

import java.util.concurrent.{ForkJoinPool, ForkJoinWorkerThread}
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.abhishek.http.HttpServer
import com.abhishek.mqtt.MqttServer
import com.abhishek.rabbitmq.{RabbitMqConnectionFactory, RabbitMqttConsumer, RabbitMqttPublisher}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn

object Boot extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = MqttExecutionContexts.mqttExecutionContext
  //HttpServer.startHttpService
  MqttServer.startServer
  val conn = RabbitMqConnectionFactory.getConnection
  RabbitMqttConsumer.init(conn)
//  for ( i <- 1 to 20) {
  //  RabbitMqttPublisher.publish(RabbitMqConnectionFactory.getThisServerID, "Hello"+i)
    //Thread.sleep(200)
  //}
}

object MqttExecutionContexts {
  implicit val mqttExecutionContext = forkJoinExecutor(20, "mqtt")
  
  def forkJoinExecutor(parallelism: Int, threadPrefix: String): ExecutionContextExecutor = {
    val factory = new ForkJoinWorkerThreadFactory() {
      override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread = {
        val worker: ForkJoinWorkerThread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
        worker.setName(s"$threadPrefix-${worker.getPoolIndex}")
        worker
      }
    }

    val pool = new ForkJoinPool(parallelism, factory, null, true) // scalastyle:ignore null
    ExecutionContext.fromExecutor(pool)
  }
}