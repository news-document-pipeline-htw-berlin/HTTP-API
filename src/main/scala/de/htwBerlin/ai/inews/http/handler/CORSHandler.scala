package de.htwBerlin.ai.inews.http.handler

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive0, RequestContext, Route, RouteResult}

import scala.concurrent.Future
import scala.concurrent.duration._

// source: https://dzone.com/articles/handling-cors-in-akka-http

trait CORSHandler {
  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With"),
    `Access-Control-Max-Age`(1.day.toMillis) // tell browser to cache OPTIONS requests
  )

  // this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }

  // this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

  // Wrap the Route with this method to enable adding of CORS headers
  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }

  // Helper method to add CORS headers to HttpResponse
  // preventing duplication of CORS headers across code
  def addCORSHeaders(response: HttpResponse):HttpResponse =
    response.withHeaders(corsResponseHeaders)
}

object CORSHandler {
  private val handler = new CORSHandler {}

  def getWithCors(route: Route): RequestContext => Future[RouteResult] = {
    options {
      handler.corsHandler(complete(StatusCodes.OK))
    } ~ get {
      route
    }
  }

  def completeWithCors(content: ToResponseMarshallable): RequestContext => Future[RouteResult] = {
    handler.corsHandler(complete(content))
  }
}
