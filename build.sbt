name := "inews-http-api"
organization := "de.htwBerlin.ai"
version := "0.1"
scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
    /* Akka http */
    "com.typesafe.akka" %% "akka-http" % "10.1.11",
    "com.typesafe.akka" %% "akka-stream" % "2.6.1",

    /* JSON Serialization */
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
    "io.spray" %% "spray-json" % "1.3.5",

    /* elastic search */
    "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % "1.1.2",
)
