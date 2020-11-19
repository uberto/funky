package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.onFailure
import com.ubertob.funky.outcome.recover
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class JsonFTest {

    @Test
    fun `JsonNode String`() {

        val expected = "abc"
        val json = JString.build(expected)

        val actual = JString.extract(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


    @Test
    fun `Json Double`() {

        val expected = 123.0
        val json = JDouble.build(expected)

        val actual = JDouble.extract(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Json Int`() {

        val expected = 124
        val json = JInt.build(expected)

        val actual = JInt.extract(json).shouldSucceed()

        expectThat(actual).isEqualTo(expected)
    }


    fun <T : Any> Outcome<*, T>.shouldSucceed(): T =
        this.onFailure { fail(it.msg) }

    fun <E : OutcomeError> Outcome<E, *>.shouldFail(): E =
        this.transform { fail("Should have failed but was $it") }
            .recover { it }
}