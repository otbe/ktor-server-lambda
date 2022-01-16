package com.mercateo.ktor.server.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.kotlintest.matchers.string.shouldHaveLength
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

fun Application.testApp() {
    routing {
        get("foo") {
            call.respond("bar")
        }
        get("large") {
            val largeResponse = StringBuilder().apply {
                repeat(8000) { append("a") }
            }.toString()

            call.respond(largeResponse)
        }
        get("empty") {
            call.respond("")
        }
    }
}

@EngineAPI
class LambdaEngineTest : AnnotationSpec() {

    private val request = mockk<APIGatewayProxyRequestEvent>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var uut: LambdaEngine

    @BeforeEach
    fun clear() {
        clearAllMocks()

        uut = LambdaEngine(createTestEnvironment())
    }

    @Test
    fun `GET request to "foo" is answered by "bar"`() {
        every { request.path } returns "/foo"
        every { request.httpMethod } returns "GET"
        lateinit var response: APIGatewayProxyResponseEvent

        uut.withTestApplication {
            response = handleRequest(request, context)
        }

        response.statusCode shouldBe 200
        response.body shouldBe "bar"
    }

    @Test
    fun `GET request to "large" is answered with large response`() {
        every { request.path } returns "large"
        every { request.httpMethod } returns "GET"
        lateinit var response: APIGatewayProxyResponseEvent


        uut.withTestApplication {
            response = handleRequest(request, context)
        }

        response.statusCode shouldBe 200
        response.body shouldHaveLength 8000
    }

    @Test
    fun `GET request to "empty" has no body`() {
        every { request.path } returns "/empty"
        every { request.httpMethod } returns "GET"
        lateinit var response: APIGatewayProxyResponseEvent

        uut.withTestApplication {
            response = handleRequest(request, context)
        }

        response.statusCode shouldBe 200
        response.body shouldBe null
    }
}

@EngineAPI
internal fun LambdaEngine.withTestApplication(
    test: LambdaEngine.() -> Unit
) {
    this.start()
    try {
        application.testApp()
        this.test()
    } finally {
        this.stop(0L, 0L)
    }
}