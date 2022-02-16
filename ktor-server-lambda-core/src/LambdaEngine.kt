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
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.streams.inputStream
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.Base64

@EngineAPI
internal class LambdaEngine(
    environment: ApplicationEngineEnvironment
) : BaseApplicationEngine(environment) {

    override fun start(wait: Boolean): LambdaEngine {
        environment.start()
        return this
    }

    override fun stop(gracePeriodMillis: Long, timeoutMillis: Long) {
        environment.stop()
    }

    fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
        runBlocking {
            val output = ByteChannel(true)

            val body = async { base64EncodeOutput(output) }

            val call = LambdaApplicationCall(
                application,
                input,
                context,
                output
            )

            // execute the user-defined application
            pipeline.execute(call)

            // flushes the current write operation
            // and afterwards does not accept any more writes
            output.close()

            APIGatewayProxyResponseEvent()
                .withBodyIfExists(body.await())
                .withStatusCode(call.response.status()?.value ?: 500)
                .withHeaders(call.response.joinedMultiValueHeaders())
        }

    private suspend fun base64EncodeOutput(output: ByteChannel): String =
        output.readRemaining().use {
            // TODO: stream bytes
            val byteArray = it.inputStream().readAllBytes()
            val b64Encoder = Base64.getEncoder()
            b64Encoder.encodeToString(byteArray)
        }

    private fun APIGatewayProxyResponseEvent.withBodyIfExists(body: String): APIGatewayProxyResponseEvent =
        if (body.isNotBlank())
            withBody(body)
                .withIsBase64Encoded(true)
        else this

}
