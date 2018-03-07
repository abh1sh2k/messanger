package com.abhishek.data

import com.zaxxer.hikari.HikariDataSource

object DataSource {
  def getDataSource() = {
    val hikariDataSource = new HikariDataSource
    hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/user_messages?allowMultiQueries=true")
    hikariDataSource.setUsername("root")
    hikariDataSource.setPassword("root")
    hikariDataSource.setMaximumPoolSize(10)
    hikariDataSource.setConnectionTimeout(5000)
    hikariDataSource.setMinimumIdle(5)
    hikariDataSource
  }
}
