package com.ubertob.funky.json

import JsonLexer
import com.ubertob.funky.expectSuccess
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

        val value = Random.nextInt()

        val jsonString = JsonNodeInt(value).render()

        val tokens = jsonLexer.tokenize(jsonString)

        val node = parseJsonNodeInt(tokens).expectSuccess()

        expectThat(node.num).isEqualTo(value)

    }
}