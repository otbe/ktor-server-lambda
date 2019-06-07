package com.mercateo.ktor.server.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.ktor.application.Application
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.server.engine.EngineAPI
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

@EngineAPI
class LambdaApplicationRequestTest : AnnotationSpec() {

    private val app = mockk<Application>(relaxed = true)
    private val call = mockk<LambdaApplicationCall>()
    private val request = mockk<APIGatewayProxyRequestEvent>()

    @BeforeEach
    fun clear() {
        clearAllMocks()

        val receivePipeline = ApplicationReceivePipeline()

        every { request.path } returns "/foo/bar"
        every { request.httpMethod } returns "GET"
        every { request.body } returns "foo"
        every { request.headers } returns mapOf(
            "X-Forwarded-For" to "localhost",
            "X-Forwarded-Proto" to "https",
            "X-Forwarded-Port" to "8080",
            "Host" to "localhost"
        )
        every { request.queryStringParameters } returns mapOf(
            "foo" to "bar",
            "bar" to "foo"
        )
        every { call.application } returns app
        every { app.receivePipeline } returns receivePipeline
    }

    @Test
    fun `headers are correctly mapped`() {
        val uut = LambdaApplicationRequest(call, request)

        uut.headers shouldBe Headers.build {
            append("X-Forwarded-For", "localhost")
            append("X-Forwarded-Proto", "https")
            append("X-Forwarded-Port", "8080")
            append("Host", "localhost")
        }
    }

    @Test
    fun `query parameters are correctly mapped`() {
        val uut = LambdaApplicationRequest(call, request)

        uut.queryParameters shouldBe Parameters.build {
            append("foo", "bar")
            append("bar", "foo")
        }
    }

    @Test
    fun `receive channel contains body`() {
        val uut = LambdaApplicationRequest(call, request)

        val buffer = ByteArray(uut.receiveChannel().availableForRead)
        runBlocking {
            uut.receiveChannel().readFully(buffer, 0, buffer.size)
        }

        String(buffer) shouldBe "foo"
    }

    @Test
    fun `connection point is configured correctly`() {
        val uut = LambdaApplicationRequest(call, request)

        uut.local.version shouldBe "HTTP/1.1"
        uut.local.host shouldBe "localhost"
        uut.local.scheme shouldBe "https"
        uut.local.port shouldBe 8080
        uut.local.remoteHost shouldBe "localhost"
        uut.local.uri shouldBe "/foo/bar"
        uut.local.method shouldBe HttpMethod.Get
    }

    @Test
    fun `connection point uses multiValueHeaders for configuration`() {
        every { request.headers } returns mapOf()
        every { request.multiValueHeaders } returns mapOf(
            "X-Forwarded-For" to listOf("localhost"),
            "X-Forwarded-Proto" to listOf("https"),
            "X-Forwarded-Port" to listOf("8080"),
            "Host" to listOf("localhost")
        )

        val uut = LambdaApplicationRequest(call, request)

        uut.local.host shouldBe "localhost"
        uut.local.scheme shouldBe "https"
        uut.local.port shouldBe 8080
        uut.local.remoteHost shouldBe "localhost"
    }

    @Test
    fun `connection point uses default value`() {
        every { request.headers } returns mapOf()
        every { request.multiValueHeaders } returns mapOf()

        val uut = LambdaApplicationRequest(call, request)

        uut.local.host shouldBe "localhost"
        uut.local.scheme shouldBe "http"
        uut.local.port shouldBe 80
        uut.local.remoteHost shouldBe ""
    }
}
