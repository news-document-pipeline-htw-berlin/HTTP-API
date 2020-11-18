name := "inews-http-api"
organization := "de.htwBerlin.ai"
version := "0.1"
scalaVersion := "2.12.1"

val akkaVersion = "10.1.11"
val elastic4sVersion = "7.3.4"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
    /* Akka http */
    "com.typesafe.akka" %% "akka-http" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % "2.6.1",

    /* Akka http CORS (Cross Origin Resource Sharing) extension
    this is need for the frontend, which runs on a different port, to access the API */
    "ch.megard" %% "akka-http-cors" % "0.4.2",

    /* JSON Serialization */
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaVersion,


    "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion, // default http client

    /* authentication */
    // https://mvnrepository.com/artifact/com.jason-goodwin/authentikat-jwt
    "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5"
)

// https://stackoverflow.com/a/39058507
assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case x => MergeStrategy.first
}