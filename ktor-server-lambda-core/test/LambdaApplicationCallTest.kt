package com.mercateo.ktor.server.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.utils.io.*
import io.mockk.every
import io.mockk.mockk

@EngineAPI
class LambdaApplicationCallTest : AnnotationSpec() {

    @Test
    fun `should add Context and RequestContext to Call attributes`() {
        val app = mockk<Application>(relaxed = true)
        val input = mockk<APIGatewayProxyRequestEvent>(relaxed = true)
        val context = mockk<Context>()
        val output = mockk<ByteChannel>()
        val requestContext = mockk<APIGatewayProxyRequestEvent.ProxyRequestContext>()
        val receivePipeline = ApplicationReceivePipeline()
        val sendPipeline = ApplicationSendPipeline()

        every { app.receivePipeline } returns receivePipeline
        every { app.sendPipeline } returns sendPipeline
        every { input.requestContext } returns requestContext

        val call = LambdaApplicationCall(app, input, context, output)

        call.attributes[LambdaContextKey] shouldBe context
        call.attributes[ProxyRequestContextKey] shouldBe requestContext
        call.attributes[BaseApplicationResponse.EngineResponseAtributeKey] shouldBe call.response
    }
}
