package com.abhishek.tcp

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import akka.actor.{Actor, ActorDSL, ActorLogging, ActorRef, ActorSystem, Props, SupervisorStrategy}
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object TcpServer extends App{
  val config = ConfigFactory.parseString("akka.loglevel = DEBUG")

  implicit val system = ActorSystem("TcpServer", config)
  system.actorOf(Props(classOf[TcpManager]), "tcp")

}

class TcpManager extends Actor with ActorLogging {

  import Tcp._
  import context.system

  //there is not recovery for broken connections
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  // bind to the listen port; the port will automatically be closed once this actor dies
  override def preStart(): Unit = {
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 1883))
  }

  // do not restart
  override def postRestart(thr: Throwable): Unit = context stop self

  def receive = {
    case Bound(localAddress) ⇒
      log.info("listening on port {}", localAddress.getPort)

    case CommandFailed(Bind(_, local, _, _, _)) ⇒
      log.warning(s"cannot bind to [$local]")
      context stop self

    //#echo-manager
    case Connected(remote, local) ⇒
      log.info("received connection from {}", remote)
      val handler: ActorRef = context.actorOf(Props(classOf[TcpConnectionHandler], sender(), remote))
      sender() ! Register(handler, keepOpenOnPeerClosed = true)


  }


}
