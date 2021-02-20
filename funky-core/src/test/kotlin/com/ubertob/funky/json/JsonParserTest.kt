package com.ubertob.funky.json

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class JsonParserTest {


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

        val node = parseJsonNodeInt(jsonString)

        expectThat(node.num).isEqualTo(value)


    }
}