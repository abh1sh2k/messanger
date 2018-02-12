package com.abhishek.mqtt

import akka.actor.ActorRef
import akka.event.{ActorEventBus, LookupClassification}

/**
  * Created by abhishek on 11/02/18
  */

final case class MqttEnvelope(topic: String, payload: Any)

class MqttEventBus extends ActorEventBus with LookupClassification{
  type Event = MqttEnvelope
  type Classifier = String
  override protected def classify(event: Event): Classifier = {
    event.topic
  }

  override protected def mapSize(): Int = 1000

  override protected def publish(event: MqttEnvelope, subscriber: ActorRef): Unit = {
    subscriber ! event.payload
  }
}
