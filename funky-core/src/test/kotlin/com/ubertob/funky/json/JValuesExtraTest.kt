package com.ubertob.funky.json

import com.ubertob.funky.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class JValuesExtraTest {

    @Test
    fun `Json Company`() {

        repeat(5) {

            val value = randomCompany()
            val json = JCompany.toJsonNode(value, NodeRoot)

            val actual = JCompany.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JCompany.toJson(value)

            expectThat(JCompany.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `Json Customer`() {

        repeat(10) {

            val value = randomCustomer()
            val json = JCustomer.toJsonNode(value, NodeRoot)

            val actual = JCustomer.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JCustomer.toJson(value)

            expectThat(JCustomer.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


}


