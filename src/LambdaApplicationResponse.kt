package com.ktor

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.response.ResponseHeaders
import io.ktor.server.engine.BaseApplicationResponse
import io.ktor.util.toMap
import kotlinx.coroutines.io.ByteChannel

internal class LambdaApplicationResponse(call: ApplicationCall, private val output: ByteChannel) :
  BaseApplicationResponse(call) {
  private var statusCode: HttpStatusCode = HttpStatusCode.OK
  private val headersNames = ArrayList<String>()
  private val headerValues = ArrayList<String>()

  override val headers = object : ResponseHeaders() {
    override fun engineAppendHeader(name: String, value: String) {
      headersNames.add(name)
      headerValues.add(value)
    }

    override fun getEngineHeaderNames(): List<String> {
      return headersNames
    }

    override fun getEngineHeaderValues(name: String): List<String> {
      val names = headersNames
      val values = headerValues
      val size = headersNames.size
      var firstIndex = -1

      for (i in 0 until size) {
        if (names[i].equals(name, ignoreCase = true)) {
          firstIndex = i
          break
        }
      }

      if (firstIndex == -1) return emptyList()

      var secondIndex = -1
      for (i in firstIndex until size) {
        if (names[i].equals(name, ignoreCase = true)) {
          secondIndex = i
          break
        }
      }

      if (secondIndex == -1) return listOf(values[firstIndex])

      val result = ArrayList<String>(size - secondIndex + 1)
      result.add(values[firstIndex])
      result.add(values[secondIndex])

      for (i in secondIndex until size) {
        if (names[i].equals(name, ignoreCase = true)) {
          result.add(values[i])
        }
      }

      return result
    }
  }

  override suspend fun respondUpgrade(upgrade: OutgoingContent.ProtocolUpgrade) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun responseChannel() = output

  override fun setStatus(statusCode: HttpStatusCode) {
    this.statusCode = statusCode
  }

  fun getApiGatewayHeaders() = headers.allValues().toMap().mapValuesTo(
    mutableMapOf()
  ) {
    it.value.first()
  }
}