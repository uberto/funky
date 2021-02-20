package com.ubertob.funky.json

import com.ubertob.funky.shouldSucceed
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BiDiJsonTest {

    @Test
    fun `JsonNode String`() {

        val expected = "abc"
        val json = JString.toJsonNode(expected)

        val actual = JString.fromJsonNode(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


    @Test
    fun `Json Double`() {

        val expected = 123.0
        val json = JDouble.toJsonNode(expected)

        val actual = JDouble.fromJsonNode(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Json Int`() {

        val expected = 124
        val json = JInt.toJsonNode(expected)

        val actual = JInt.fromJsonNode(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


}