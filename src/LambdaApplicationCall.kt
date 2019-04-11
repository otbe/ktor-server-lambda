package com.ktor

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.ktor.application.Application
import io.ktor.server.engine.BaseApplicationCall
import io.ktor.server.engine.EngineAPI
import kotlinx.coroutines.io.ByteChannel

@EngineAPI
internal class LambdaApplicationCall(
  application: Application,
  _request: APIGatewayProxyRequestEvent,
  output: ByteChannel
) :
  BaseApplicationCall(application) {
  override val response = LambdaApplicationResponse(this, output)

  override val request = LambdaApplicationRequest(this, _request)

  init {
    putResponseAttribute()
  }
}