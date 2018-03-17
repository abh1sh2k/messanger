package com.abhishek.mqtt

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import com.abhishek.common.{Connect => MqttConnect, _}
import com.abhishek.rabbitmq.{RabbitMqConnectionFactory, RabbitMqttPublisher}
import scodec.Attempt.Failure
import scodec.bits.BitVector

import scala.concurrent.ExecutionContext

//#echo-handler
object TcpConnectionHandler {
  final case class Ack(offset: Int) extends Tcp.Event
  val eventBus = new MqttEventBus
  val userDataService = new com.abhishek.data.UserDataServiceImpl
  def props(connection: ActorRef, remote: InetSocketAddress): Props =
    Props(classOf[TcpConnectionHandler], connection, remote)
}

class TcpConnectionHandler(connection: ActorRef, remote: InetSocketAddress)
  extends Actor with ActorLogging {

  import Tcp._
  import TcpConnectionHandler._
  import com.abhishek.main.MqttExecutionContexts.mqttExecutionContext
  val redisClient = com.abhishek.data.RedisClient.getRedisClient()
  var uid : Option[String] = None
  // sign death pact: this actor terminates when connection breaks
  //context watch connection

  def connected : Receive = {
    case Received(data) => {
      val b = PacketsHelper.decode(BitVector(data))
      b.foreach {
        case Left(p: Packet) => handlePacketAfterConnected(p)
        case Right(p: Failure) => log.warning("failure " + p)
      }
    }
    case MqttEnvelope(_,payload) =>
      if(payload.isInstanceOf[Packet])
        send(payload.asInstanceOf[Packet])
    case p :Packet => //this packet coming from event bus
      println("packet from event bus")
      send(p)
    case PeerClosed     ⇒ {
      println("closed messages");
      die
    }
    case Connected => { log.warning("getting connected packed even after connected"); die }
    case x :Any => { log.warning("unexpected message received ",x)}
  }

  def receive = {
    case Received(data) ⇒
      val b = PacketsHelper.decode(BitVector(data))
      b.foreach {
        case Left(p: Packet) => {
          log.warning("recieved " + p)
          p match {
            case c: MqttConnect =>
              if(c.client_id == null)
                die
              else {
                uid = Some(c.client_id)
                redisClient.registerClientOnThisServer(c.client_id)
                send (Connack (Header (false, 0, false), 0) )
                userDataService.getOfflineMessage(c.client_id).map{
                  _.map(message => send(Publish(c.header, c.client_id, 1, message.message)))
                }
                context become connected
              }
            case Subscribe(header, messageIdentifier, topics) =>
              if(uid.isDefined) topics.foreach{ topic => eventBus.subscribe(self, topic._1) }
              else die
            case _ => log.warning("wrong packet")
          }
        }
        case Right(p: Failure) => {
          log.warning("failure " + p)
          die
        }
      }

  }

  def send(packet: Packet) = {
    val a = PacketsHelper.encode(packet)
    val envelope = ByteString(a.require.toByteArray)
    connection ! Write(envelope)
  }

  def handlePacketAfterConnected(p : Packet ): Unit = {
    log.info("handle packet " + p)
    p match {
      case MqttConnect(header, connect_flags, client_id, topic, message, user, password) =>
        if (! eventBus.clientOnThisServer(client_id))
          die
      case Subscribe(header, messageIdentifier, topics) =>
        topics.foreach{
          topic =>
            eventBus.subscribe(self, topic._1)
        }
        send( Suback(header, messageIdentifier, topics.map(_._2) ))
      case p@Publish(header: Header, topic: String, message_identifier: Int, payload: String) =>
        if (eventBus.clientOnThisServer(topic))
          //client is connected on this server
          eventBus.publish(MqttEnvelope(topic, p))
        else{
          //currently topic == clientId as each client listen to its own topic only
          redisClient.getClientServer(topic).map{
            fTopic => fTopic match {
               case Some(server) =>
                 if(RabbitMqConnectionFactory.getThisServerID.equals(server)){ //wrong server is not connected know
                   redisClient.unregisterClient(topic)
                   userDataService.storeOfflineMessage(payload, topic, message_identifier)
                 }
                 else {
                   //send message to that server
                   RabbitMqttPublisher.publish(server , payload)
                 }
               case None => // client is not connected to any server
                 userDataService.storeOfflineMessage(payload, topic, message_identifier)
             }

          }
        }

      case Disconnect(_) => die
      case Pingreq(header) => {} //send(Pingresp(Header(false, 0, false)))
    }
  }

  def die() = {
    uid match {
      case Some(topic) =>
        uid = None
        eventBus.unsubscribe(self,topic)
        redisClient.unregisterClient(topic)
      case _ => eventBus unsubscribe self
    }

    context stop self
  }

}