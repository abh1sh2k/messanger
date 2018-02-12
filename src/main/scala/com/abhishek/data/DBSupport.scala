package com.abhishek.data

import java.sql.{Connection, PreparedStatement, ResultSet}

import akka.actor.FSM.Failure

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Success, Try}

case class Query(sql: String, params: Seq[Any] = Nil, timeout: Int = 1)


object DBSupport {
  lazy val dataSource = DataSource.getDataSource()
}

trait DBSupport {
  def getStatement(q: Query)(implicit con: Connection): PreparedStatement = {
    val stmt = con prepareStatement q.sql
    q.params.zipWithIndex foreach { case (p, i) => stmt.setObject(i + 1, p) }
    stmt.setQueryTimeout(q.timeout)
    stmt
  }

  def getConnection() : Connection = DBSupport.dataSource.getConnection

}
