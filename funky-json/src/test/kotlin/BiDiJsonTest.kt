package com.ubertob.funky.json

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BiDiJsonTest {

    @Test
    fun `JsonString Customer and back`() {

        val expected = Customer(123, "abc")
        val json = toJsonString(expected, JCustomer)

        val actual = fromJsonString(json, JCustomer).expectSuccess()

        expectThat(actual).isEqualTo(expected)
    }


    @Test
    fun `JsonString Product and back`() {


        val jsonToothpaste = toJsonString(toothpaste, JProduct)
        val jsonOffer = toJsonString(offer, JProduct)

//        println(jsonToothpaste)
//        println(jsonOffer)

        val actualToothpaste = fromJsonString(jsonToothpaste, JProduct).expectSuccess()
        val actualOffer = fromJsonString(jsonOffer, JProduct).expectSuccess()

        expectThat(actualToothpaste).isEqualTo(toothpaste)
        expectThat(actualOffer).isEqualTo(offer)
    }



    @Test
    fun `JsonString Invoice and back`() {

        val json = toJsonString(invoice, JInvoice)

        val actual = fromJsonString(json, JInvoice).expectSuccess()

        expectThat(actual).isEqualTo(invoice)
    }

    @Test
    fun `parsing illegal json gives us precise errors`() {
        val illegalJson =
            "{\"id\":1001,\"vat-to-pay\":true,\"customer\":{\"id\":1,\"name\":\"ann\"},\"items\":[{\"id\":1001,\"desc\":\"toothpaste \\\"whiter than white\\\"\",\"price:12.34},{\"id\":10001,\"desc\":\"special offer\"}],\"total\":123.45}"

        val error = fromJsonString(illegalJson, JInvoice).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing reason: Unexpected character at position 140: 'i' (ASCII: 105)'")
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

        val error = fromJsonString(jsonWithDifferentField, JInvoice).expectFailure()

        expectThat(error.msg).isEqualTo("error at </items/1> reason: Not found long_description")
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
      "price": "125"
    },
    {
      "id": 10001,
      "short_desc": "special offer"
    }
  ],
  "total": 123.45
}  """.trimIndent()

        val error = fromJsonString(jsonWithDifferentField, JInvoice).expectFailure()

        expectThat(error.msg).isEqualTo("error at </items/0/price> reason: Expected Double but found JsonNodeString(text=125, path=[items, 0, price])")
    }
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


//todo
// random tests
// parsing arrays, and single values
// removing klaxon
// add passing node path in serialization as well
// add pre-check for multiple failures in parsing
// add test for multiple reuse
// add tests for concurrency reuse
// add test for different kind of failures