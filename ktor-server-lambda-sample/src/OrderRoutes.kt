/*
 * Copyright © 2019 Mercateo AG (http://www.mercateo.com)
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


import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

val orders = mutableListOf(Order("1", listOf(Article("1", "Paper"))))

fun Routing.orders() {
  route("orders") {
    get {
      call.respond(orders)
    }

    post {
      val order = call.receive<Order>()

      orders.add(order)

      call.respond(HttpStatusCode.NoContent)
    }

    get("{id}") {
      val order = orders.find { it.id == call.parameters["id"] } ?: return@get call.respond(HttpStatusCode.NotFound)

      call.respond(order)
    }

    delete("{id}") {
      val order =
        orders.find { it.id == call.parameters["id"] } ?: return@delete call.respond(HttpStatusCode.NotFound)

      orders.remove(order)

      call.respond(HttpStatusCode.NoContent)
    }
  }
}

