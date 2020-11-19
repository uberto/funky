package com.ubertob.funky.hof

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun start(s: String, x: Int) = s.substring(x)
fun last(x: Int, s: String) = s.substring(x)
fun String.startR(x: Int) = substring(x)
fun three(s: String, x: Int, c: Char) = s.substring(x) + c

internal class CurryingTest {


    @Test
    fun `applying first parameter`() {
        val words = listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(::last applyFirst 2)

        expectThat(words).isEqualTo(listOf("EAD", "EESE", "EAK"))
    }

    @Test
    fun `applying last parameter`() {
        val words = listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(::start applyLast 2)

        expectThat(words).isEqualTo(listOf("EAD", "EESE", "EAK"))
    }


    @Test
    fun `applying receiver parameter`() {
        val words = listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(String::startR applyThis 2)
        expectThat(words).isEqualTo(listOf("EAD", "EESE", "EAK"))
    }

    @Test
    fun `applying 3 parameters`() {
        val words = listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(::three applyLast3 'a' applyLast 1)
        expectThat(words).isEqualTo(listOf("READa", "HEESEa", "TEAKa"))
    }
}

