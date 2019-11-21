package de.htwBerlin.ai.inews

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.http.HttpRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends App {
  implicit val system: ActorSystem = ActorSystem("demo-system")
  implicit val materializer: ActorMaterializer.type = ActorMaterializer

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  // base route, serve index.html
  /*val baseRoute = getFromFile("frontend/build/index.html")
  // static route, serve static files (images, css,images, css, js) js)
  val publicRoute = pathPrefix("static") (getFromDirectory("frontend/build/static"))*/

  val routes = new HttpRoutes()(executionContext)

  val bindingFuture = Http().bindAndHandle(routes.route, host, port)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine // wait until user input
  bindingFuture
    .flatMap(_.unbind) // trigger unbinding from port
    .onComplete(_ => system.terminate) // shutdown when done
}
