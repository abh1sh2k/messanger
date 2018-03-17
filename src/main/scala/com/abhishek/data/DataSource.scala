package com.abhishek.data

import com.abhishek.conf.MqttConfig
import com.zaxxer.hikari.HikariDataSource

object DataSource {
  def getDataSource() = {
    val hikariDataSource = new HikariDataSource
    val url = "jdbc:mysql://%s:3306/user_messages?allowMultiQueries=true"
    hikariDataSource.setJdbcUrl(String.format(url, MqttConfig.mysqlHost))
    hikariDataSource.setUsername("root")
    hikariDataSource.setPassword("root")
    hikariDataSource.setMaximumPoolSize(20)
    hikariDataSource.setConnectionTimeout(5000)
    hikariDataSource.setMinimumIdle(5)
    hikariDataSource
  }
}
