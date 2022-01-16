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
import io.ktor.server.engine.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
            val body = StringBuilder()

            val outputConsumerJob = launch {
                output.readRemaining().use {
                    it.readerUTF8().readText().let(body::append)
                }
            }

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
            outputConsumerJob.join()

            APIGatewayProxyResponseEvent()
                .withBodyIfExists(body.toString())
                .withStatusCode(call.response.status()?.value ?: 500)
                .withHeaders(call.response.joinedMultiValueHeaders())
        }

    private fun APIGatewayProxyResponseEvent.withBodyIfExists(body: String): APIGatewayProxyResponseEvent {
        if (body.isNotBlank())
            withBody(body)
        return this
    }
}
