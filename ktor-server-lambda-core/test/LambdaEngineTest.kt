package com.mercateo.ktor.server.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.EngineAPI
import io.ktor.server.testing.createTestEnvironment
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.TimeUnit

fun Application.testApp() {
    routing {
        get("foo") {
            call.respond("bar")
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
        this.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }
}