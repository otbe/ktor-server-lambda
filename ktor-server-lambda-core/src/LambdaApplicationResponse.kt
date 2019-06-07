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

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.response.ResponseHeaders
import io.ktor.server.engine.BaseApplicationResponse
import kotlinx.coroutines.io.ByteChannel

internal class LambdaApplicationResponse(
    call: ApplicationCall,
    private val output: ByteChannel
) : BaseApplicationResponse(call) {

    override fun setStatus(statusCode: HttpStatusCode) {
        // we don't need the http status ATM
    }

    override suspend fun responseChannel() = output

    fun joinedMultiValueHeaders() = multiHeaderMap.mapValuesTo(mutableMapOf()) {
        it.value.joinToString()
    }

    private val multiHeaderMap = mutableMapOf<String, MutableList<String>>()

    override val headers = object : ResponseHeaders() {

        override fun engineAppendHeader(name: String, value: String) {
            if (multiHeaderMap[name].isNullOrEmpty()) multiHeaderMap[name] =
                mutableListOf(value) else multiHeaderMap[name]?.add(value)
        }

        override fun getEngineHeaderNames(): List<String> = multiHeaderMap.keys.toList()

        override fun getEngineHeaderValues(name: String): List<String> = multiHeaderMap[name]?.toList() ?: emptyList()
    }

    /**
     * Upgrading an HTTP-Connection in a AWS Lambda environment doesn't make sense.
     * As described here (https://developer.mozilla.org/en-US/docs/Web/HTTP/Protocol_upgrade_mechanism)
     * this connection type is specific to WebSockets, which in turn can only be used in API Gateway connections.
     * For more information on how to use WebSockets with AWS Lambda,
     * see e.g. this blog post: https://serverless.com/blog/api-gateway-websockets-support/
     */
    override suspend fun respondUpgrade(upgrade: OutgoingContent.ProtocolUpgrade) =
        throw UnsupportedOperationException("Upgrading HTTP/1.1 connections not supported.")
}
