package com.ubertob.funky.json

class PeekingIterator<T>(val innerIterator: Iterator<T>) : Iterator<T> {

    private var next: T? = null

    private var counter = 0

    fun peek(): T = next ?: run {
        val nn = innerIterator.next()
        next = nn
        nn
    }

    override fun hasNext(): Boolean = next != null || innerIterator.hasNext()

    override fun next(): T = (next ?: advanceIterator()).also { next = null }

    fun position(): Int = counter

    private fun advanceIterator(): T = innerIterator.next().also { counter++ }
}

fun <T> Sequence<T>.peekingIterator(): PeekingIterator<T> = PeekingIterator(iterator())