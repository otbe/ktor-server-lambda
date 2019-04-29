package com.mercateo.ktor.server.lambda

import com.google.gson.GsonBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import kotlin.test.assertEquals


class ApplicationTest {

  @Test
  fun testRoot() {
    withTestApplication({ main() }) {

      handleRequest(HttpMethod.Get, "/orders") {
        addHeader("Content-Type", "application/json")
      }.apply {
        val gson = GsonBuilder().setPrettyPrinting().create()
        assertEquals(HttpStatusCode.OK, response.status())
        assertEquals(gson.toJson(orders), response.content)
      }
    }
  }

}

