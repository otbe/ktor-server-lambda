package com.ktor

import com.mercateo.ktor.server.lambda.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import kotlin.test.assertEquals


class ApplicationTest {

  @Test
  fun testRoot() {
    withTestApplication({ module(testing = true) }) {

      handleRequest(HttpMethod.Post, "/graphql") {

        addHeader("Content-Type", "application/json")

        setBody(
          """
          {
            "name": "boo"
          }
        """.trimIndent()
        )

      }.apply {

        assertEquals(HttpStatusCode.OK, response.status())
        assertEquals("Hello boo!", response.content)

      }
    }
  }

}
