package com.abhishek.mqtt

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import com.abhishek.common.{Connect => MqttConnect, _}
import scodec.Attempt.Failure
import scodec.bits.BitVector

//#echo-handler
object TcpConnectionHandler {
  final case class Ack(offset: Int) extends Tcp.Event
  val eventBus = new MqttEventBus
  def props(connection: ActorRef, remote: InetSocketAddress): Props =
    Props(classOf[TcpConnectionHandler], connection, remote)
}

class TcpConnectionHandler(connection: ActorRef, remote: InetSocketAddress)
  extends Actor with ActorLogging {

  import Tcp._
  import TcpConnectionHandler._
  // sign death pact: this actor terminates when connection breaks
  //context watch connection

  def connected : Receive = {
    case Received(data) => {
      println("recieved data ",data)
      val b = PacketsHelper.decode(BitVector(data))
      b.foreach {
        case Left(p: Packet) => {
          log.warning("recieved packet " + p)
          handlePacket(p)
        }
        case Right(p: Failure) => {
          log.warning("failure " + p)
        }
      }
    }
    case MqttEnvelope(_,payload) =>
      if(payload.isInstanceOf[Packet])
        send(payload.asInstanceOf[Packet])
    case p :Packet => //this packet coming from event bus
      send(p)
    case PeerClosed     ⇒ {
      println("closed messages");
      die
    }

  }

  def receive = {
    case Received(data) ⇒
      val b = PacketsHelper.decode(BitVector(data))
      b.foreach {
        case Left(p: Packet) => {
          log.warning("recieved " + p)
          p match {
            case c: MqttConnect =>
              println(" uid ", c.client_id)
              if(c.client_id == null)
                context stop self
              else {
                send (Connack (Header (false, 0, false), 0) ) //val mqttHandler: ActorRef = context.actorOf(Props(classOf[MqttConnectionHandler]))
                context become connected
              }
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

  def handlePacket(p : Packet ) = {
    println("packet recieved ", p)
    p match {
      case MqttConnect(header, connect_flags, client_id, topic, message, user, password) =>
        println("still getting connected messages")
        die
      case Subscribe(header, messageIdentifier, topics) =>
        topics.foreach{
          topic =>
            eventBus.subscribe(self, topic._1)
        }
        send( Suback(header, messageIdentifier, topics.map(_._2) ))
      case p@Publish(header: Header, topic: String, message_identifier: Int, payload: String) =>
        eventBus.publish(MqttEnvelope(topic, p))

      case Pingreq(header) => send(Pingresp(Header(false, 0, false)))
    }
  }

  def die() = {
    eventBus unsubscribe self
    context stop self
  }

}