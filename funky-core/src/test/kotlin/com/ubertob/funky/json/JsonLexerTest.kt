package com.ubertob.funky.json

import JsonLexer
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class JsonLexerTest {

    val lexer = JsonLexer()

    @Test
    fun `single word`() {
        val json = "abc"
        val seq = lexer.tokenize(json)

        expectThat(seq.toList()).isEqualTo(listOf(json))
    }

    @Test
    fun `spaces tab and new lines word`() {
        val json = "  abc   def\ngh\tijk\r lmn \n\n opq"
        val seq = lexer.tokenize(json)

        expectThat(seq.toList()).isEqualTo(
            listOf(
                "abc", "def", "gh", "ijk", "lmn", "opq"
            )
        )
    }

    @Test
    fun `json special tokens`() {
        val json = "[]{}:,  [a,b,c]  {d:e}"
        val seq = lexer.tokenize(json)

        expectThat(seq.toList()).isEqualTo(
            listOf(
                "[", "]", "{", "}", ":", ",", "[", "a", ",", "b", ",", "c", "]", "{", "d", ":", "e", "}"
            )
        )
    }

    @Test
    fun `json strings`() {
        val json = """
            { "abc": 123}
        """.trimIndent()
        val seq = lexer.tokenize(json)

        expectThat(seq.toList()).isEqualTo(
            listOf(
                "{", "\"", "abc", "\"", ":", "123", "}"
            )
        )
    }
}