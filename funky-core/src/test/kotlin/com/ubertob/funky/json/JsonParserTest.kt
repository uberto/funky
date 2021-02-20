package com.ubertob.funky.json

import JsonLexer
import com.ubertob.funky.expectSuccess
import com.ubertob.funky.lowercase
import com.ubertob.funky.randomString
import com.ubertob.funky.text
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class JsonParserTest {

    val jsonLexer = JsonLexer()

    @Test
    fun `render Int`() {
        val value = 123

        val jsonString = JsonNodeInt(value).render()

        expectThat(jsonString).isEqualTo("123")
    }

    @Test
    fun `parse Int`() {
        repeat(10) {

            val value = Random.nextInt()

            val jsonString = JsonNodeInt(value).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeInt(tokens).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }
    }

    @Test
    fun `render Long`() {
        val value = Long.MAX_VALUE

        val jsonString = JsonNodeLong(value).render()

        expectThat(jsonString).isEqualTo("9223372036854775807")
    }

    @Test
    fun `parse Long`() {
        repeat(10) {

            val value = Random.nextLong()

            val jsonString = JsonNodeLong(value).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeLong(tokens).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }
    }

    @Test
    fun `render Boolean`() {
        val value = true

        val jsonString = JsonNodeBoolean(value).render()

        expectThat(jsonString).isEqualTo("true")
    }

    @Test
    fun `parse Boolean`() {

        repeat(3) {

            val value = Random.nextBoolean()

            val jsonString = JsonNodeBoolean(value).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeBoolean(tokens).expectSuccess()

            expectThat(node.value).isEqualTo(value)
        }
    }

    @Test
    fun `render Double`() {
        val value = Double.MIN_VALUE

        val jsonString = JsonNodeDouble(value).render()

        expectThat(jsonString).isEqualTo("4.9E-324")
    }

    @Test
    fun `parse Double`() {

        repeat(10) {

            val value = Random.nextDouble()

            val jsonString = JsonNodeDouble(value).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeDouble(tokens).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }

    }

    @Test
    fun `render String`() {
        val value = """ abc {} \\ " \n 123"""

        val jsonString = JsonNodeString(value).render()

        expectThat(jsonString).isEqualTo("""" abc {} \\ \" \n 123"""")
    }

    @Test
    fun `parse simple String`() {

        repeat(10) {
            val value = randomString(lowercase, 3, 3)

            val jsonString = JsonNodeString(value).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeString(tokens).expectSuccess()

            expectThat(node.text).isEqualTo(value)
        }
    }

    @Test
    fun `parse String`() {

        repeat(100) {
            val value = randomString(text, 1, 10)

            val jsonString = JsonNodeString(value).render()

//            println("$value -> $jsonString")

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeString(tokens).expectSuccess()

            expectThat(node.text).isEqualTo(value)
        }
    }

    @Test
    fun `render null`() {
        val jsonString = JsonNodeNull().render()

        expectThat(jsonString).isEqualTo("null")
    }

    @Test
    fun `parse Null`() {

        val jsonString = JsonNodeNull().render()

        val tokens = jsonLexer.tokenize(jsonString)

        parseJsonNodeNull(tokens).expectSuccess()

    }

    //todo array and object
}