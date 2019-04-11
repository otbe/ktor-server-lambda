package com.ktor


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.routing
import io.ktor.server.engine.EngineAPI


@KtorExperimentalLocationsAPI
@Location("/graphql")
data class GraphQLRequest(val name: String = "")

@KtorExperimentalLocationsAPI
@Suppress("unused")
fun Application.module() {
  install(Locations)
  install(DefaultHeaders)

  routing {
    post<GraphQLRequest> {
      val s = call.receive<GraphQLRequest>()
      println(s.name)

      call.respondText("HELLO WORLD!2", contentType = ContentType.Text.Plain)
    }
  }

  install(ContentNegotiation) {
    gson {
      setPrettyPrinting()
    }
  }
}

@EngineAPI
val adapter = LambdaAdapter()


@EngineAPI
fun handler(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
  adapter.handle(input, context)


fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)