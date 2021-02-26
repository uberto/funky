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
            val value = randomCustomer()
            val json = JCustomer.toJsonNode(value, NodeRoot)

            val actual = JCustomer.fromJsonNode(json).expectSuccess()

            expectThat(actual).isEqualTo(value)

            val jsonStr = JCustomer.toJson(value)

            expectThat(JCustomer.fromJson(jsonStr).expectSuccess()).isEqualTo(value)
        }
    }


    @Test
    fun `json array of Customers`() {

        repeat(10) {
            val jsonUserArray = JArray(JCustomer)

            val value = randomList(0, 10) { randomCustomer() }

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


val toothpaste = Product(1001, "paste", "toothpaste \"whiter than white\"", 12.34)
val offer = Product(10001, "special offer", "offer for custom fidality", null)

private fun randomCustomer() = Customer(Random.nextInt(1, 1000), randomString(text, 1, 10))

private fun randomProduct() = Product(
    Random.nextInt(1, 1000),
    randomString(text, 2, 10),
    randomText(100),
    randomNullable { randomPrice(10, 1000) })

private fun randomInvoice() = Invoice(
    id = InvoiceId(randomString(digits, 5, 5)),
    vat = Random.nextBoolean(),
    customer = randomCustomer(),
    items = randomList(1, 10) { randomProduct() },
    total = randomPrice(10, 1000)
)


data class Customer(val id: Int, val name: String)

object JCustomer : JAny<Customer>() {

    val id by JField(Customer::id, JInt)
    val name by JField(Customer::name, JString)

    override fun JsonNodeObject.deserialize() =
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

    override fun JsonNodeObject.deserialize() =
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

    override fun JsonNodeObject.deserialize(): Invoice =
        Invoice(
            id = +id,
            vat = +vat,
            customer = +customer,
            items = +items,
            total = +total
        )

}


//todo
// add common JBidi (JInstant, JSealed JEnum etc.)
// check with Java
// recheck for all unchecked cast
// add prettyPrint/compactPrint options
// add null/skipField option
// add parseJson from Reader
// serialize with stringbuilder
// add tests for concurrency reuse
// measure performance against other libs
// add un-typed option JObject<Any>
// add constant fields