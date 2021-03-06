package com.abhishek.mqtt

import java.util.Comparator

import akka.actor.ActorRef
import akka.event.{ActorEventBus, LookupClassification}
import akka.util.Index

/**
  * Created by abhishek on 11/02/18
  */

final case class MqttEnvelope(topic: String, payload: Any)

class MqttEventBus extends ActorEventBus with LookupClassification {
  type Event = MqttEnvelope
  type Classifier = String

  override protected def classify(event: Event): Classifier = {
    event.topic
  }

  override protected def mapSize(): Int = 1000

  override protected def publish(event: MqttEnvelope, subscriber: ActorRef): Unit = {
    subscriber ! event.payload
  }
  def clientOnThisServer(client : String) = subscribers.valueIterator(client).hasNext
}
