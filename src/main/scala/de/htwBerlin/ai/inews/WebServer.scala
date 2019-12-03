package de.htwBerlin.ai.inews

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.http.HttpRoutes

import scala.concurrent.ExecutionContextExecutor

object WebServer extends App {
  implicit val system: ActorSystem = ActorSystem("demo-system")
  implicit val materializer: ActorMaterializer.type = ActorMaterializer

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val routes = new HttpRoutes()(executionContext)

  val bindingFuture = Http().bindAndHandle(routes.route, host, port)
}
