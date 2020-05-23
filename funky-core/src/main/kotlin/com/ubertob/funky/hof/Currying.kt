package com.ubertob.funky.hof

fun <A, B, C> ((A, B) -> C).curry(): (A) -> (B) -> C = { x -> { y -> this(x, y) } }

fun <A, B, C, D> ((A, B, C) -> D).curry(): (A) -> (B) -> (C) -> D = { x -> { y -> { z -> this(x, y, z) } } }

fun <A, B, C, D, E> ((A, B, C, D) -> E).curry(): (A) -> (B) -> (C) -> (D) -> E = { w -> { x -> { y -> { z -> this(w, x, y, z) } } } }


infix fun <A, B> ((A) -> B).`@`(a: A): B = this(a)  //  `@`(this, a)

infix fun <A, B, C> ((A, B) -> C).applyLast(b: B): (A) -> C = { a -> this(a, b) }
infix fun <A, B, C, D> ((A, B, C) -> D).applyLast3(c: C): (A, B) -> D = { a, b -> this(a, b, c) }

infix fun <A, B, C> ((A, B) -> C).applyFirst(a: A): (B) -> C = { b -> this(a, b) }

infix fun <A, B, C> (A.(B) -> C).applyThis(b: B): (A) -> C = { a -> a.this(b) }
