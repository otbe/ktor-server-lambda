package com.mercateo.ktor.server.lambda

import io.kotlintest.be
import io.kotlintest.matchers.contain
import io.kotlintest.matchers.containAll
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.ktor.application.Application
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.header
import io.ktor.server.engine.EngineAPI
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.io.ByteChannel

@EngineAPI
class LambdaApplicationResponseTest : AnnotationSpec() {
    private val app = mockk<Application>(relaxed = true)
    private val call = mockk<LambdaApplicationCall>()
    private val output = mockk<ByteChannel>()

    @BeforeEach
    fun clear() {
        clearAllMocks()

        val receivePipeline = ApplicationReceivePipeline()
        val sendPipeline = ApplicationSendPipeline()

        every { call.application } returns app
        every { app.receivePipeline } returns receivePipeline
        every { app.sendPipeline } returns sendPipeline

    }

    @Test
    fun `should let me set a new Status`() {
        val response = LambdaApplicationResponse(call, output)

        response.status(HttpStatusCode.Conflict)
        response.status() shouldBe HttpStatusCode.Conflict
    }

    @Test
    fun `should let me add headers`() {
        val response = LambdaApplicationResponse(call, output)

        response.header("foo", "bar")
        response.header("baz", "foo")
        response.header("baz", "bar")

        response.headers.allValues().contains("foo") should be(true)
        response.headers.allValues().contains("baz") should be(true)
        response.headers.allValues().contains("foo2") should be(false)

        response.headers.values("baz") should containAll("foo", "bar")
    }

    @Test
    fun `should let get a comma joined list of multiple values iof the same header`() {
        val response = LambdaApplicationResponse(call, output)

        response.header("foo", "bar")
        response.header("baz", "foo")
        response.header("baz", "bar")

        response.joinedMultiValueHeaders() should contain("foo", "bar")
        response.joinedMultiValueHeaders() should contain("baz", "foo, bar")
    }
}