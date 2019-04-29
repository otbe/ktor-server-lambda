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
import com.mercateo.ktor.server.lambda.LambdaAdapter
import io.ktor.server.engine.EngineAPI

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@EngineAPI
val adapter = LambdaAdapter()


@Suppress("unused")
@EngineAPI
fun handle(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
  adapter.handle(input, context)
