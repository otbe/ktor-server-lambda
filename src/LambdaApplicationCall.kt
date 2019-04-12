package com.ktor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.ktor.application.Application
import io.ktor.server.engine.BaseApplicationCall
import io.ktor.server.engine.EngineAPI
import io.ktor.util.AttributeKey
import kotlinx.coroutines.io.ByteChannel

val LambdaContextKey = AttributeKey<Context>("LambdaContextKey")
val ProxyRequestContextKey = AttributeKey<APIGatewayProxyRequestEvent.ProxyRequestContext>("ProxyRequestContextKey")

@EngineAPI
internal class LambdaApplicationCall(
  application: Application,
  input: APIGatewayProxyRequestEvent,
  context: Context,
  output: ByteChannel
) :
  BaseApplicationCall(application) {
  override val response = LambdaApplicationResponse(this, output)

  override val request = LambdaApplicationRequest(this, input)

  init {
    putResponseAttribute()
    attributes.put(LambdaContextKey, context)
    attributes.put(ProxyRequestContextKey, input.requestContext)
  }
}