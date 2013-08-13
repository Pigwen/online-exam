name := "online-exam"

organization := "maodian.org"

version := "0.1"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

EclipseKeys.withSource := true

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "org.slf4j" % "slf4j-nop" % "1.7.5",
  "mysql" % "mysql-connector-java" % "5.1.26"
)
