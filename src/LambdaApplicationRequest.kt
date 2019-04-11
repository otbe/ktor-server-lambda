package com.ktor

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.ktor.application.ApplicationCall
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.RequestCookies
import io.ktor.server.engine.BaseApplicationRequest
import kotlinx.coroutines.io.ByteReadChannel

class LambdaApplicationRequest(
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