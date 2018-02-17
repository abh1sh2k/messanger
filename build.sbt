name := "messanger"
version := "1.0"
scalaVersion := "2.11.0"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "New Motion Repository" at "http://nexus.thenewmotion.com/content/groups/public/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.6",
  "com.zaxxer" % "HikariCP" % "2.7.2",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.7",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
  "com.zaxxer" % "HikariCP" % "2.7.2",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.7",
  "com.livestream" %% "scredis" % "2.0.6",
  "com.thenewmotion" %% "akka-rabbitmq" % "3.0.0",
  "org.scodec" %% "scodec-core" % "1.7.1",
  "org.scodec" %% "scodec-bits" % "1.0.9")


mainClass in assembly := Some("com.abhishek.tcp.TcpServer")