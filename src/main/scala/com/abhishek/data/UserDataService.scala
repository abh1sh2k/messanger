package com.abhishek.data

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
    val q = Query("insert into user values(?,?) ", List[String](msidn, createuid(msidn)))
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

  def createuid(msisdn: String) = msisdn

}