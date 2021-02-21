package com.ubertob.funky.json

import com.ubertob.funky.*
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class BiDiJsonTest {

    @Test
    fun `JsonNode String`() {
        repeat(10) {
            val value = randomString(lowercase, 3, 3)

            val json = JString.toJsonNode(value)

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
            val json = JDouble.toJsonNode(value)

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
            val json = JInt.toJsonNode(value)

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
            val json = JLong.toJsonNode(value)

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
            val json = JBoolean.toJsonNode(value)

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

            val node = jsonStringArray.toJsonNode(value)

            val actual = jsonStringArray.fromJsonNode(node).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = jsonStringArray.toJson(value)

            expectThat(jsonStringArray.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `Json Customer and back`() {

        repeat(10) {
            val value = Customer(Random.nextInt(), randomString(text, 1, 10))
            val json = JCustomer.toJsonNode(value)

            val actual = JCustomer.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JCustomer.toJson(value)

            expectThat(JCustomer.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `json array of Customers`() {

        val jsonUserArray = JArray(JCustomer)

        val value = listOf(
            Customer(1, "Adam"),
            Customer(2, "Bob"),
            Customer(3, "Carol")
        )

        val node = jsonUserArray.toJsonNode(value)

        val actual = jsonUserArray.fromJsonNode(node).expectSuccess()

        expectThat(actual).isEqualTo(value)
    }


    @Test
    fun `Json with nullable and back`() {

        val toothpasteJson = JProduct.toJsonNode(toothpaste)
        val offerJson = JProduct.toJsonNode(offer)

        val actualToothpaste = JProduct.fromJsonNode(toothpasteJson).expectSuccess()
        val actualOffer = JProduct.fromJsonNode(offerJson).expectSuccess()

        expect {
            that(actualToothpaste).isEqualTo(toothpaste)
            that(actualOffer).isEqualTo(offer)
        }
    }


    @Test
    fun `Json with objects inside and back`() {

        val json = JInvoice.toJsonNode(invoice)

        val actual = JInvoice.fromJsonNode(json).expectSuccess()

        expectThat(actual).isEqualTo(invoice)
    }


    @Test
    fun `Customer serialize and deserialize`() {

        val customer = Customer(123, "abc")
        val jsonNodeObject = JCustomer.toJsonNode(customer)

        val actual = JCustomer.fromJsonNode(jsonNodeObject).expectSuccess()

        expectThat(actual).isEqualTo(customer)
    }

    val ann = Customer(1, "ann")
    val toothpaste = Product(1001, "paste", "toothpaste \"whiter than white\"", 12.34)
    val offer = Product(10001, "special offer", "offer for custom fidality", null)
    val invoice = Invoice(InvoiceId("1001"), true, ann, listOf(toothpaste, offer), 123.45)


    data class Customer(val id: Int, val name: String)

    object JCustomer : JAny<Customer>() {

        val id by JField(Customer::id, JInt)
        val name by JField(Customer::name, JString)

        override fun JsonNodeObject.tryDeserialize() =
            Customer(
                id = +id,
                name = +name
            )
    }


    data class Product(val id: Int, val shortDesc: String, val longDesc: String, val price: Double?)

    object JProduct : JAny<Product>() {

        val id by JField(Product::id, JInt)
        val long_description by JField(Product::longDesc, JString)
        val short_desc by JField(Product::shortDesc, JString)
        val price by JFieldMaybe(Product::price, JDouble)

        override fun JsonNodeObject.tryDeserialize() =
            Product(
                id = +id,
                shortDesc = +short_desc,
                longDesc = +long_description,
                price = +price
            )
    }


    data class InvoiceId(override val raw: String) : StringWrapper


    data class Invoice(
        val id: InvoiceId,
        val vat: Boolean,
        val customer: Customer,
        val items: List<Product>,
        val total: Double
    )

    object JInvoice : JAny<Invoice>() {
        val id by JField(Invoice::id, JStringWrapper(::InvoiceId))
        val vat by JField(Invoice::vat, JBoolean, jsonFieldName = "vat-to-pay")
        val customer by JField(Invoice::customer, JCustomer)
        val items by JField(Invoice::items, JArray(JProduct))
        val total by JField(Invoice::total, JDouble)

        override fun JsonNodeObject.tryDeserialize(): Invoice =
            Invoice(
                id = +id,
                vat = +vat,
                customer = +customer,
                items = +items,
                total = +total
            )

    }

}


//todo
// parsing objects
// removing klaxon
// add position + after on parsing errors
// recheck for all unchecked cast
// improve failure error msg (failing test)
// check with Unicode chars
// add prettyPrint/compactPrint options
// add parseJson from Reader
// add common JBidi (JInstant, JSealed etc.)
// add passing node path in serialization as well (for errors)
// add pre-check for multiple failures in parsing
// add test for multiple reuse
// add tests for concurrency reuse
// add more tests for different kind of failures
// measure performance against other libs
// add un-typed option JObject<Any>