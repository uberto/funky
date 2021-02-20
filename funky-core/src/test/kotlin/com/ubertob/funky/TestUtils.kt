package com.ubertob.funky

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.onFailure
import com.ubertob.funky.outcome.recover
import org.junit.jupiter.api.fail

import kotlin.random.Random

fun <T : Any> Outcome<*, T>.expectSuccess(): T =
    this.onFailure { fail(it.msg) }

fun <E : OutcomeError> Outcome<E, *>.expectFailure(): E =
    this.transform { fail("Should have failed but was $it") }
        .recover { it }


const val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val lowercase = "abcdefghijklmnopqrstuvwxyz"
const val digits = "0123456789"
const val spacesigns = " ,.:+-()%$@{}[]\"\n\r"
const val text = lowercase + digits + spacesigns

fun stringsGenerator(charSet: String, minLen: Int, maxLen: Int): Sequence<String> = generateSequence {
    randomString(charSet, minLen, maxLen)
}

fun randomString(charSet: String, minLen: Int, maxLen: Int) =
    StringBuilder().run {
        val len = if (maxLen > minLen) Random.nextInt(maxLen - minLen) + minLen else minLen
        repeat(len) {
            append(charSet.random())
        }
        toString()
    }

fun randomText(len: Int) = randomString(text, len, len)

