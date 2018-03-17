FROM openjdk:8
ENV SBT_VERSION 0.13.16
RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /app
ADD target/scala-2.11/messanger-assembly-1.0.jar /app
ADD src/main/resources/docker.conf /app
EXPOSE 1883
CMD java -Dconfig.file=docker.conf -jar messanger-assembly-1.0.jar
