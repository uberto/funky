package com.ubertob.funky.json

import com.ubertob.funky.*
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class JsonAdjunctionTest {

    @Test
    fun `JsonNode String`() {
        repeat(10) {
            val value = randomString(lowercase, 3, 3)

            val json = JString.toJsonNode(value, NodeRoot)

            val actual = JString.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JString.toJson(value)

            expectThat(JString.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `Json Double`() {
        repeat(10) {

            val value = Random.nextDouble()
            val json = JDouble.toJsonNode(value, NodeRoot)

            val actual = JDouble.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JDouble.toJson(value)

            expectThat(JDouble.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `Json Int`() {

        repeat(10) {

            val value = Random.nextInt()
            val json = JInt.toJsonNode(value, NodeRoot)

            val actual = JInt.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JInt.toJson(value)

            expectThat(JInt.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `Json Long`() {

        repeat(10) {

            val value = Random.nextLong()
            val json = JLong.toJsonNode(value, NodeRoot)

            val actual = JLong.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JLong.toJson(value)

            expectThat(JLong.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `Json Boolean`() {

        repeat(3) {

            val value = Random.nextBoolean()
            val json = JBoolean.toJsonNode(value, NodeRoot)

            val actual = JBoolean.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JBoolean.toJson(value)

            expectThat(JBoolean.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }

    @Test
    fun `json array of Strings`() {

        repeat(10) {
            val jsonStringArray = JArray(JString)

            val value = randomList(0, 5) { randomString(text, 1, 10) }

            val node = jsonStringArray.toJsonNode(value, NodeRoot)

            val actual = jsonStringArray.fromJsonNode(node).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = jsonStringArray.toJson(value)

            expectThat(jsonStringArray.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `Json Customer and back`() {

        repeat(10) {
            val value = randomPerson()
            val json = JPerson.toJsonNode(value, NodeRoot)

            val actual = JPerson.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JPerson.toJson(value)

            expectThat(JPerson.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `json array of Customers`() {

        repeat(10) {
            val jsonUserArray = JArray(JPerson)

            val value = randomList(0, 10) { randomPerson() }

            val node = jsonUserArray.toJsonNode(value, NodeRoot)

            val actual = jsonUserArray.fromJsonNode(node).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = jsonUserArray.toJson(value)

            expectThat(jsonUserArray.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `Json with nullable and back`() {

        val toothpasteJson = JProduct.toJsonNode(toothpaste, NodeRoot)
        val offerJson = JProduct.toJsonNode(offer, NodeRoot)

        val actualToothpaste = JProduct.fromJsonNode(toothpasteJson).expectSuccess()
        val actualOffer = JProduct.fromJsonNode(offerJson).expectSuccess()

        expect {
            that(actualToothpaste).isEqualTo(toothpaste)
            that(actualOffer).isEqualTo(offer)
        }

        listOf(toothpaste, offer).forEach { prod ->
            val jsonStr = JProduct.toJson(prod)

            expectThat(JProduct.fromJson(jsonStr).expectSuccess()).isEqualTo(prod)
        }
    }


    @Test
    fun `Json with objects inside and back`() {

        repeat(100) {
            val invoice = randomInvoice()
            val json = JInvoice.toJsonNode(invoice, NodeRoot)

            val actual = JInvoice.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(invoice)

            val jsonStr = JInvoice.toJson(invoice)

            expectThat(JInvoice.fromJson(jsonStr).expectSuccess()).isEqualTo(invoice)
        }
    }

    @Test
    fun `parsing illegal Int gives us precise errors`() {
        val illegalJson = "123b"

        val error = JInt.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected an Int at position 1 but found '123b' while parsing <[root]>")
    }

    @Test
    fun `parsing illegal json gives us precise errors`() {
        val illegalJson =
            "{\"id\":1001,\"vat-to-pay\":true,\"customer\":{\"id\":1,\"name\":\"ann\"},\"items\":[{\"id\":1001,\"desc\":\"toothpaste \\\"whiter than white\\\"\",\"price:12.34},{\"id\":10001,\"desc\":\"special offer\"}],\"total\":123.45}"

        val error = JInvoice.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected '\"' at position 7 but found '1001' while parsing <[root]/id>")
    }

    @Test
    fun `parsing json without a field return precise errors`() {
        val jsonWithDifferentField =
            """
 {
  "id": "1001",
  "vat-to-pay": true,
  "customer": {
    "id": 1,
    "name": "ann"
  },
  "items": [
    {
      "id": 1001,
      "short_desc": "toothpaste",
      "long_description": "toothpaste \"whiter than white\"",
      "price": 125
    },
    {
      "id": 10001,
      "short_desc": "special offer"
    }
  ],
  "total": 123.45
}  """.trimIndent()

        val error = JInvoice.fromJson(jsonWithDifferentField).expectFailure()

        expectThat(error.msg).isEqualTo("error at <[root]/items/1>: Not found long_description")
    }


    @Test
    fun `parsing json with different type of fields return precise errors`() {
        val jsonWithDifferentField =
            """
 {
  "id": "1001",
  "vat-to-pay": true,
  "customer": {
    "id": 1,
    "name": "ann"
  },
  "items": [
    {
      "id": 1001,
      "short_desc": "toothpaste",
      "long_description": "toothpaste \"whiter than white\"",
      "price": "a string"
    },
    {
      "id": 10001,
      "short_desc": "special offer"
    }
  ],
  "total": 123.45
}  """.trimIndent()

        val error = JInvoice.fromJson(jsonWithDifferentField).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected a Double at position 55 but found '\"' while parsing <[root]/items/0/price>")
    }
}


//todo
// add tests for JInstantD, JLocalDate, JCurrency, JInstance and JMap
// improve errors + add tests for other json errors
// add test example with Java
// add prettyPrint/compactPrint options
// add null/skipField option
// add parseJson from Reader
// serialize with stringbuilder
// add tests for concurrency reuse
// measure performance against other libs
// add un-typed option JObject<Any>
// add constant fields (ignoring Json content)