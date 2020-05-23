package com.ubertob.funky.outcome

import org.junit.jupiter.api.Test

internal class OutcomeTest {

    data class Err(override val msg: String) : OutcomeError

    data class User(val name: String, val email: String)

    fun getUser(id: Int): Outcome<OutcomeError, User> = if (id > 0) User("u$id", "$id@example.com").asSuccess() else Err("wrong id").asFailure()

    fun getMailText(name: String): Outcome<OutcomeError, String> = if (name.isEmpty()) Err("no name").asFailure() else "Hello $name".asSuccess()

    fun sendEmailUser(email: String, text: String): Outcome<OutcomeError, Unit> = if (text.isNotEmpty() && email.isNotEmpty()) Unit.asSuccess() else Err("empty text or email").asFailure()


    @Test
    fun bindingComposition() {

        val res = Do {

            sendEmailUser("123@a.com", "bye bye")
        }
    }


    @Test
    fun bindingComposition2() {

        val res = Do {

            val u = getUser(123)()
            val t = getMailText(u.name)()
            sendEmailUser(u.email, t)()

        }.result
    }


    @Test
    fun bindingComposition3() {

        val res = Do {

            val u = +getUser(123)
            val t = +getMailText(u.name)
            +sendEmailUser(u.email, t)

        }.result
    }


    class Do<T : Any>(val f: Do<*>.() -> T) {

        operator fun <E : OutcomeError, T : Any> Outcome<E, T>.unaryPlus(): T =
                when (this) {
                    is Success -> value
                    is Failure -> throw WithMonadsException(error)
                }

        operator fun <E : OutcomeError, T : Any> Outcome<E, T>.invoke(): T =
                when (this) {
                    is Success -> value
                    is Failure -> throw WithMonadsException(error)
                }

        data class WithMonadsException(val error: OutcomeError) : Exception()

        val result: Outcome<OutcomeError, T>
            get() =
                try {
                    f().asSuccess()
                } catch (e: WithMonadsException) {
                    e.error.asFailure()
                }

    }

}