package com.ktor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.ktor.server.engine.EngineAPI
import io.ktor.server.engine.commandLineEnvironment

@EngineAPI
class LambdaAdapter(args: Array<String> = emptyArray()) {
  private var engine: LambdaEngine

  init {
    val applicationEnvironment = commandLineEnvironment(args)
    engine = LambdaEngine(applicationEnvironment)
    engine.start(false)
  }

  fun handle(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
    return engine.handleRequest(input, context)
  }
}



