package com.ubertob.funky.json

import JsonLexer
import com.ubertob.funky.expectSuccess
import com.ubertob.funky.lowercase
import com.ubertob.funky.randomString
import com.ubertob.funky.text
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.random.Random

class JsonParserTest {

    val jsonLexer = JsonLexer()


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
    fun `render exp Num`() {
        val value = Double.MIN_VALUE

        val jsonString = JsonNodeNum(value.toBigDecimal(), NodeRoot).render()

        expectThat(jsonString).isEqualTo("4.9E-324")
    }

    @Test
    fun `render decimal Num`() {
        val num = "123456789123456789.01234567890123456789"
        val value = BigDecimal(num)

        val jsonString = JsonNodeNum(value, NodeRoot).render()

        expectThat(jsonString).isEqualTo(num)
    }

    @Test
    fun `render integer Num`() {
        val value = Int.MAX_VALUE.toDouble()

        val jsonString = JsonNodeNum(value.toBigDecimal(), NodeRoot).render()

        expectThat(jsonString).isEqualTo("2147483647")
    }

    @Test
    fun `parse Num`() {

        repeat(10) {

            val value = Random.nextDouble().toBigDecimal()

            val jsonString = JsonNodeNum(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeNum(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }

        repeat(10) {

            val value = Random.nextLong().toBigDecimal()

            val jsonString = JsonNodeNum(value, NodeRoot).render()

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeNum(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }

        repeat(10) {

            val value = Random.nextLong().toBigDecimal().pow(10)

            val jsonString = JsonNodeNum(value, NodeRoot).render()

            println("$value -> $jsonString")

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeNum(tokens, NodeRoot).expectSuccess()

            expectThat(node.num).isEqualTo(value)
        }

        repeat(10) {

            val value = Random.nextDouble().toBigDecimal().pow(10)

            val jsonString = JsonNodeNum(value, NodeRoot).render()

            println("$value -> $jsonString")

            val tokens = jsonLexer.tokenize(jsonString)

            val node = parseJsonNodeNum(tokens, NodeRoot).expectSuccess()

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
            mapOf("id" to JsonNodeNum(123.toBigDecimal(), NodeRoot), "name" to JsonNodeString("Ann", NodeRoot)),
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
            NodeRoot,
            mapOf("id" to ::parseJsonNodeNum, "name" to ::parseJsonNodeString)
        ).expectSuccess()

        val expected = """{"id": 123, "name": "Ann"}"""
        expectThat(nodes.render()).isEqualTo(expected)
    }
}