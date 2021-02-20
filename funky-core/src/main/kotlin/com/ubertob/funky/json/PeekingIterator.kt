package com.ubertob.funky.json

class PeekingIterator<T>(val innerIterator: Iterator<T>) : Iterator<T> {

    var next: T? = null

    fun peek(): T = next ?: run {
        val nn = innerIterator.next()
        next = nn
        nn
    }

    override fun hasNext(): Boolean = next != null || innerIterator.hasNext()

    override fun next(): T = (next ?: innerIterator.next()).also { next = null }
}

fun <T> Sequence<T>.peekingIterator(): PeekingIterator<T> = PeekingIterator(iterator())