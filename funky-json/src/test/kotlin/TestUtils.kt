package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.onFailure
import com.ubertob.funky.outcome.recover
import org.junit.jupiter.api.fail


fun <T : Any> Outcome<*, T>.shouldSucceed(): T =
    this.onFailure { fail(it.msg) }

fun <E : OutcomeError> Outcome<E, *>.shouldFail(): E =
    this.transform { fail("Should have failed but was $it") }
        .recover { it }
