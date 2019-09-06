package com.ubertob.funky.hof

fun <A,B,C> ((A, B) -> C).curry(): (A) -> (B) -> C = {x -> { y -> this(x, y)}}

fun <A,B,C,D> ((A, B, C) -> D).curry(): (A) -> (B) -> (C) -> D = {x -> { y -> { z -> this(x, y, z)}}}

fun <A,B,C,D, E> ((A, B, C, D) -> E).curry(): (A) -> (B) -> (C) -> (D) -> E  = {w -> {x -> { y -> { z -> this(w, x, y, z)}}}}



infix fun <A,B> ((A) -> B).`@`(a: A): B =   this(a)  //  `@`(this, a)