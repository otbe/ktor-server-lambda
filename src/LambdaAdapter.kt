package com.ktor

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.ktor.application.Application
import io.ktor.server.engine.*
import io.ktor.util.pipeline.execute
import io.ktor.util.toMap
import kotlinx.coroutines.io.ByteChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

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

@EngineAPI
class LambdaEngine(
  environment: ApplicationEngineEnvironment
) : BaseApplicationEngine(environment) {

  override fun start(wait: Boolean): LambdaEngine {
    environment.start()
    return this
  }

  override fun stop(gracePeriod: Long, timeout: Long, timeUnit: TimeUnit) {
    environment.stop()
  }

  fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
    runBlocking {
      val output = ByteChannel(true)
      val call = LambdaApplicationCall(application, input, output)

      pipeline.execute(call)

      val buffer = ByteArray(output.availableForRead)
      output.readFully(buffer, 0, buffer.size)
      output.close()

      APIGatewayProxyResponseEvent()
        .withStatusCode(call.response.status()!!.value)
        .withHeaders(
          call.response.headers.allValues().toMap().mapValuesTo(
            mutableMapOf<String, String>()
          ) {
            it.value.first()
          }
        )
        .withBody(String(buffer))
    }
}

@EngineAPI
class LambdaApplicationCall(application: Application, _request: APIGatewayProxyRequestEvent, output: ByteChannel) :
  BaseApplicationCall(application) {
  override val response = LambdaApplicationResponse(this, output)

  override val request = LambdaApplicationRequest(this, _request)

  init {
    putResponseAttribute()
  }
}
