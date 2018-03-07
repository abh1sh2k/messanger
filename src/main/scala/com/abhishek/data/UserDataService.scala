package com.abhishek.data

import java.sql.ResultSet

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

case class UserMessages(message:String ,userid:String)
trait UserDataService {
  def isUserExist(msidn: String): Boolean

  def createUser(msidn: String)(implicit ex: ExecutionContext): Future[Int]
}

class UserDataServiceImpl extends UserDataService with DBSupport {

  override def isUserExist(msidn: String): Boolean = {
    val q = Query("select * from user where msidn =  ? ", List[String](msidn))
    implicit val conn = getConnection()
    val stmt = getStatement(q)
    try {
      getStatement(q).executeQuery().next()
    }
    catch {
      case e : Exception => println("Ex ",e.getMessage)
        false
    }
    finally {
      stmt.close()
      conn.close()
    }
  }

  override def createUser(msidn: String)(implicit ex: ExecutionContext): Future[Int] = {
    val uuid = createuid(msidn)
    val q = Query("insert into user values(?,?) ", List[String](msidn, uuid))
    implicit val conn = getConnection()
    val stmt = getStatement(q)
    Future {
      try {
        stmt.executeUpdate()
      }
      catch {
        case e : Exception => println("Ex ",e.getMessage)
          -1
      }
      finally {
        stmt.close()
        conn.close()
      }
    }
  }

  private def createuid(msisdn: String) = msisdn

  def storeOfflineMessage(message : String , clientId : String ,identifier : Int )(implicit executionContext: ExecutionContext):Unit = {
    val q = Query("insert into messages(message,userid,message_identifier) values (? , ? , ?)" , Seq(message, clientId , identifier))
    implicit val conn = getConnection()
    val stmt = getStatement(q)
    Future {
      try {
        stmt.executeUpdate()
      }
      catch {
        case e : Exception => println("Ex ",e.getMessage)
          -1
      }
      finally {
        stmt.close()
        conn.close()
      }
    }
  }

  def getOfflineMessage(clientId : String )(implicit executionContext: ExecutionContext): Future[List[UserMessages]] = {
    //
    val q = Query("select * from messages where userid = ?; delete from messages where userid = ? " , Seq(clientId ,clientId))
    implicit val conn = getConnection()
    val stmt = getStatement(q)
    var rsList = List.empty[ResultSet]
    Future {
      try {
        val rs = stmt.executeQuery()
        getMessages(rs)
      }
      catch {
        case e : Exception => println("Ex ",e.getMessage); List.empty[UserMessages]
      }
      finally {
        rsList.map(_.close())
        stmt.close()
        conn.close()
      }
    }
  }
  def getMessages(rs: ResultSet): List[UserMessages] = {
    Iterator.continually((rs.next(), rs)).takeWhile(_._1).map(r => UserMessages(r._2.getString("message"),
      r._2.getString("userid"))).toList

  }
}
