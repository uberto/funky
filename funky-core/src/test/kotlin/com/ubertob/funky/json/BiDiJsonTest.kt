package com.ubertob.funky.json

import com.ubertob.funky.shouldSucceed
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BiDiJsonTest {

    @Test
    fun `JsonNode String`() {

        val expected = "abc"
        val json = JString.toJson(expected)

        val actual = JString.fromJson(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


    @Test
    fun `Json Double`() {

        val expected = 123.0
        val json = JDouble.toJson(expected)

        val actual = JDouble.fromJson(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Json Int`() {

        val expected = 124
        val json = JInt.toJson(expected)

        val actual = JInt.fromJson(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


}