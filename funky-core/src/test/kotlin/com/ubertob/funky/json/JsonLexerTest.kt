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
}