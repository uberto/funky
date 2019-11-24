package com.ubertob.funky.nullable


fun <T: Any, U: Any> T?.ifNotNull(f: (T) -> U): U? = TODO()


fun <U: Any> CharSequence?.ifNotNullOrEmpty(f: (CharSequence) -> U): U? = TODO()