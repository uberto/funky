package com.ubertob.funky.hof

import org.junit.jupiter.api.Test

fun start(s: String, x: Int) = s.substring(x)
fun last(x: Int, s: String) = s.substring(x)
fun String.startR(x: Int) = substring(x)
fun three(s: String, x: Int, c: Char) = s.substring(x) + c

internal class CurryingTest {

    @Test
    fun applying() {
        listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(::start applyLast 2)
                .map(::last applyFirst 2)
                .map(String::startR applyThis 2)
                .map(::three applyLast3 'a' applyLast 1)
    }
}

