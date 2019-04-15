/*
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
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
import io.ktor.application.ApplicationCall
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.RequestCookies
import io.ktor.server.engine.BaseApplicationRequest
import kotlinx.coroutines.io.ByteReadChannel

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

  override val cookies = RequestCookies(this)

  override val local: RequestConnectionPoint = object : RequestConnectionPoint {
    override val scheme: String
      get() = "https"

    override val version: String
      get() = "1"

    override val uri: String
      get() = request.path

    override val host: String
      get() = request.headers["Host"]?.toString()?.substringBefore(":") ?: "localhost"

    override val port: Int
      get() = request.headers["Host"]?.toString()?.substringAfter(":", "80")?.toInt() ?: 80

    override val method: HttpMethod
      get() = HttpMethod.parse(request.httpMethod)

    override val remoteHost: String
      get() = "unknown" // TODO
  }

  override fun receiveChannel() = ByteReadChannel(request.body)
}