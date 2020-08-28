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

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.engine.*
import io.ktor.utils.io.*

internal class LambdaApplicationRequest(
    call: ApplicationCall,
    private val request: APIGatewayProxyRequestEvent
) : BaseApplicationRequest(call) {

    override val headers = Headers.build {
        request.headers?.forEach { (key, value) ->
            append(key, value)
        }
    }

    override val queryParameters = Parameters.build {
        request.queryStringParameters?.forEach { (key, value) ->
            append(key, value)
        }
    }

    override val local: RequestConnectionPoint = LambdaRequestConnectionPoint(request)

    override val cookies = RequestCookies(this)

    override fun receiveChannel() = ByteReadChannel(request.body)
}

private data class LambdaRequestConnectionPoint(
    private val request: APIGatewayProxyRequestEvent
) : RequestConnectionPoint {

    override val scheme: String
        get() = request.headers["X-Forwarded-Proto"]
            ?: request.multiValueHeaders["X-Forwarded-Proto"]?.getOrNull(0)
            ?: "http"

    // this information is not included in APIGatewayProxyRequestEvent, so always reply with HTTP version 1.1
    override val version: String
        get() = "HTTP/1.1"

    override val uri: String
        get() = request.path

    override val host: String
        get() = request.headers["Host"]
            ?: request.multiValueHeaders["Host"]?.getOrNull(0)
            ?: "localhost"

    override val port: Int
        get() = request.headers["X-Forwarded-Port"]?.toInt()
            ?: request.multiValueHeaders["X-Forwarded-Port"]?.getOrNull(0)?.toIntOrNull()
            ?: 80

    override val method: HttpMethod
        get() = HttpMethod.parse(request.httpMethod)

    override val remoteHost: String
        get() = request.headers["X-Forwarded-For"]
            ?: request.multiValueHeaders["X-Forwarded-For"]?.getOrNull(0)
            ?: ""
}
