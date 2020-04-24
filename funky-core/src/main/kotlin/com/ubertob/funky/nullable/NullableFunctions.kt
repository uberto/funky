package com.ubertob.funky.nullable


fun <T: Any, U: Any> T?.ifNotNull(f: (T) -> U): U? = this?.let(f)

fun <U: Any> CharSequence?.ifNotNullOrEmpty(f: (CharSequence) -> U): U? = this?.let { if (it.isNotEmpty()) f(it) else null }