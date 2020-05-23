package com.ubertob.funky.outcome

sealed class Outcome<out E : OutcomeError, out T : Any> {

    fun <U : Any> mapSuccess(f: (T) -> U): Outcome<E, U> =
            when (this) {
                is Success -> Success(f(this.value))
                is Failure -> this
            }

    fun <F : OutcomeError> mapFailure(f: (E) -> F): Outcome<F, T> =
            when (this) {
                is Success -> this
                is Failure -> Failure(f(this.error))
            }

    fun <U : Any> mapBoth(fVal: (T) -> U, fErr: (E) -> U): U =
            mapSuccess(fVal)
                    .recover(fErr)

    companion object {
        fun <T : Any> tryThis(block: () -> T): Outcome<ThrowableError, T> =
                try {
                    Success(block())
                } catch (e: Throwable) {
                    Failure(ThrowableError(e))
                }
    }
}

data class Success<T : Any>(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError>(val error: E) : Outcome<E, Nothing>()

fun <T : Any, U : Any, E : OutcomeError> Outcome<E, T>.lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> = { this.mapSuccess { f(it) } }

inline fun <T : Any, U : Any, E : OutcomeError> Outcome<E, T>.bindSuccess(f: (T) -> Outcome<E, U>): Outcome<E, U> =
        when (this) {
            is Success<T> -> f(value)
            is Failure<E> -> this
        }

inline fun <T : Any, F : OutcomeError, E : OutcomeError> Outcome<E, T>.bindFailure(f: (E) -> Outcome<F, T>): Outcome<F, T> =
        when (this) {
            is Success<T> -> this
            is Failure<E> -> f(error)
        }

inline fun <T : Any, E : OutcomeError> Outcome<E, T>.recover(fRec: (E) -> T): T =
        when (this) {
            is Success -> value
            is Failure -> fRec(error)
        }

inline fun <E : OutcomeError, T : Any> Outcome<E, T>.mapNullableError(f: (T) -> E?): Outcome<E, Unit> =
        when (this) {
            is Success<T> -> {
                val error = f(this.value)
                if (error == null) Unit.asSuccess() else error.asFailure()
            }
            is Failure<E> -> this
        }

inline fun <T : Any, E : OutcomeError> Outcome<E, T>.exitOnFailure(block: (E) -> Nothing): T =
        when (this) {
            is Success<T> -> value
            is Failure<E> -> block(error)
        }


inline fun <T : Any, E : OutcomeError> Outcome<E, T>.failIf(predicate: (T) -> Boolean, error: E): Outcome<E, T> =
        when (this) {
            is Success<T> -> if (predicate(value)) error.asFailure() else this
            is Failure<E> -> this
        }


interface OutcomeError {
    val msg: String
}

data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String
        get() = t.message.orEmpty()
}

fun <T : OutcomeError> T.asFailure(): Outcome<T, Nothing> = Failure(this)
fun <T : Any> T.asSuccess(): Outcome<Nothing, T> = Success(this)