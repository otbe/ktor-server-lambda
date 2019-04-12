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
import io.ktor.application.Application
import io.ktor.server.engine.BaseApplicationCall
import io.ktor.server.engine.EngineAPI
import kotlinx.coroutines.io.ByteChannel

@EngineAPI
internal class LambdaApplicationCall(
  application: Application,
  _request: APIGatewayProxyRequestEvent,
  output: ByteChannel
) :
  BaseApplicationCall(application) {
  override val response = LambdaApplicationResponse(this, output)

  override val request = LambdaApplicationRequest(this, _request)

  init {
    putResponseAttribute()
  }
}