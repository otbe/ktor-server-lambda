/*
 * Copyright © 2019 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.ktor.server.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.BaseApplicationEngine
import io.ktor.server.engine.EngineAPI
import io.ktor.util.pipeline.execute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.io.ByteChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

@EngineAPI
internal class LambdaEngine(
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
      val call = LambdaApplicationCall(
        application,
        input,
        context,
        output
      )

      pipeline.execute(call)

      val buffer = ByteArray(output.availableForRead)
      output.readFully(buffer, 0, buffer.size)
      output.close()

      APIGatewayProxyResponseEvent().apply {
        withStatusCode(call.response.status()?.value ?: 500)

        withHeaders(
          call.response.getApiGatewayHeaders()
        )

        // TODO https://github.com/otbe/ktor-server-lambda/issues/10
        if (buffer.isNotEmpty()) {
          withBody(String(buffer))
        }
      }
    }
}