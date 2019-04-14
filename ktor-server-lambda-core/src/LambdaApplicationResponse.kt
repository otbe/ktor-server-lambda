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

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.response.ResponseHeaders
import io.ktor.server.engine.BaseApplicationResponse
import kotlinx.coroutines.io.ByteChannel

internal class LambdaApplicationResponse(call: ApplicationCall, val output: ByteChannel) :
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
}