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

        val jsonString = JsonNodeInt(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo("123")
    }

    @Test
    fun `parse Int`() {
        repeat(10) {

            val value = Random.nextInt()

            val jsonString = JsonNodeInt(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeInt(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }
    }

    @Test
    fun `render Long`() {
        val value = Long.MAX_VALUE

        val jsonString = JsonNodeLong(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo("9223372036854775807")
    }

    @Test
    fun `parse Long`() {
        repeat(10) {

            val value = Random.nextLong()

            val jsonString = JsonNodeLong(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeLong(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }
    }

    @Test
    fun `render Boolean`() {
        val value = true

        val jsonString = JsonNodeBoolean(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo("true")
    }

    @Test
    fun `parse Boolean`() {

        repeat(3) {

            val value = Random.nextBoolean()

            val jsonString = JsonNodeBoolean(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeBoolean(tokens, NodeRoot).expectSuccess()

            expectThat(node.value).isEqualTo(value)
        }
    }

    @Test
    fun `render Double`() {
        val value = Double.MIN_VALUE

        val jsonString = JsonNodeDouble(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo("4.9E-324")
    }

    @Test
    fun `parse Double`() {

        repeat(10) {

            val value = Random.nextDouble()

            val jsonString = JsonNodeDouble(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeDouble(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }

    }

    @Test
    fun `render String`() {
        val value = """ abc {} \\ " \n 123"""

        val jsonString = JsonNodeString(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo("""" abc {} \\ \" \n 123"""")
    }

    @Test
    fun `parse simple String`() {

        repeat(10) {
            val value = randomString(lowercase, 3, 3)

            val jsonString = JsonNodeString(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeString(tokens, NodeRoot).expectSuccess()

            expectThat(node.text).isEqualTo(value)
        }
    }

    @Test
    fun `parse String`() {

        repeat(100) {
            val value = randomString(text, 1, 10)

            val jsonString = JsonNodeString(value, NodeRoot).render()

//            println("$value -> $jsonString")

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeString(tokens, NodeRoot).expectSuccess()

            expectThat(node.text).isEqualTo(value)
        }
    }

    @Test
    fun `render null`() {
        val jsonString = JsonNodeNull(NodeRoot).render()

        expectThat(jsonString).isEqualTo("null")
    }

    @Test
    fun `parse Null`() {

        val jsonString = JsonNodeNull(NodeRoot).render()

        val tokens = jsonLexer.tokenize(jsonString)

        parseJsonNodeNull(tokens, NodeRoot).expectSuccess()

    }

    @Test
    fun `render array`() {
        val jsonString =
            JsonNodeArray(listOf(JsonNodeString("abc", NodeRoot), JsonNodeString("def", NodeRoot)), NodeRoot).render()

        expectThat(jsonString).isEqualTo("""["abc", "def"]""")
    }

    @Test
    fun `parse array`() {

        val jsonString = """
            ["abc", "def"]
        """.trimIndent()

        val tokens = jsonLexer.tokenize(jsonString)

        val nodes = parseJsonNodeArray(tokens, ::parseJsonNodeString, NodeRoot).expectSuccess()

        expectThat(nodes.render()).isEqualTo("""["abc", "def"]""")
    }

    @Test
    fun `render object`() {
        val jsonString = JsonNodeObject(
            mapOf("id" to JsonNodeInt(123, NodeRoot), "name" to JsonNodeString("Ann", NodeRoot)),
            NodeRoot
        ).render()

        val expected = """{"id": 123, "name": "Ann"}"""
        expectThat(jsonString).isEqualTo(expected)
    }

    @Test
    fun `parse an object`() {

        val jsonString = """
          {
            "id": 123,
            "name": "Ann"
          }
        """

        val tokens = jsonLexer.tokenize(jsonString)

        val nodes = parseJsonNodeObject(
            tokens,
            mapOf("id" to ::parseJsonNodeInt, "name" to ::parseJsonNodeString),
            NodeRoot
        ).expectSuccess()

        val expected = """{"id": 123, "name": "Ann"}"""
        expectThat(nodes.render()).isEqualTo(expected)
    }
}