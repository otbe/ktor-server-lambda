package com.mercateo.ktor.server.lambda

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotlintest.specs.FreeSpec

class LambdaApplicationRequestTest : FreeSpec({
    fun testRoot() {
        assertThat(true).isEqualTo(true)
    }
})
