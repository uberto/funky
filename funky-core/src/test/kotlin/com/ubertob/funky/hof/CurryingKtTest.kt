package com.ubertob.funky.hof

import org.junit.jupiter.api.Test

internal class CurryingTest {

    fun start(s: String, x: Int) = s.substring(x)

    @Test
    fun applying() {


        listOf("bread", "cheese", "steak")
                .map(String::toUpperCase)
                .map(::start apply 2)
    }
}