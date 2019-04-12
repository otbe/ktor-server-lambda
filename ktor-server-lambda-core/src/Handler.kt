/*
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.ktor.server.lambda

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
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.EngineAPI

fun Application.module() {
  install(DefaultHeaders)

  install(ContentNegotiation) {
    gson {
      setPrettyPrinting()
    }
  }

  routing {

    post("/graphql") {
      val s = call.receive<GraphQLRequest>()
      println(s.name)

      call.respondText("HELLO WORLD!2", contentType = ContentType.Text.Plain)
    }

  }

}

data class GraphQLRequest(val name: String = "")

@EngineAPI
val adapter = LambdaAdapter()


@EngineAPI
fun handler(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
  adapter.handle(input, context)


fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)