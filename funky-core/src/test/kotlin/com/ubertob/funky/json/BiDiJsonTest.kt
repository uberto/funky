package com.ubertob.funky.json

import com.ubertob.funky.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BiDiJsonTest {

    @Test
    fun `JsonNode String`() {

        val expected = "abc"
        val json = JString.toJsonNode(expected)

        val actual = JString.fromJsonNode(json).expectSuccess()

        expectThat(actual).isEqualTo(expected)
    }


    @Test
    fun `Json Double`() {

        val expected = 123.0
        val json = JDouble.toJsonNode(expected)

        val actual = JDouble.fromJsonNode(json).expectSuccess()

        expectThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Json Int`() {

        val expected = 124
        val json = JInt.toJsonNode(expected)

        val actual = JInt.fromJsonNode(json).expectSuccess()

        expectThat(actual).isEqualTo(expected)
    }


}