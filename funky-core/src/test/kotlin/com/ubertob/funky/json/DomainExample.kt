package com.ubertob.funky.json

import com.ubertob.funky.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.random.Random


val toothpaste = Product(1001, "paste", "toothpaste \"whiter than white\"", 12.34)
val offer = Product(10001, "special offer", "offer for custom fidality", null)

fun randomPerson() = Person(Random.nextInt(1, 1000), randomString(text, 1, 10))
fun randomCompany() = Company(randomString(lowercase, 5, 10), TaxType.values().random())

fun randomCustomer(): Customer = when (Random.nextBoolean()) {
    true -> randomPerson()
    false -> randomCompany()
}

fun randomProduct() = Product(
    Random.nextInt(1, 1000),
    randomString(text, 2, 10),
    randomText(100),
    randomNullable { randomPrice(10, 1000) })

fun randomInvoice() = Invoice(
    id = InvoiceId(randomString(digits, 5, 5)),
    vat = Random.nextBoolean(),
    customer = randomPerson(),
    items = randomList(1, 10) { randomProduct() },
    total = BigDecimal(randomPrice(10, 1000)),
    created = LocalDate.now(),
    paid = randomNullable { Instant.now().truncatedTo(SECONDS) }
)

sealed class Customer()
data class Person(val id: Int, val name: String) : Customer()
data class Company(val name: String, val taxType: TaxType) : Customer()


object JPerson : JAny<Person>() {

    private val id by JField(Person::id, JInt)
    private val name by JField(Person::name, JString)

    override fun JsonNodeObject.deserializeOrThrow() =
        Person(
            id = +id,
            name = +name
        )
}


data class Product(val id: Int, val shortDesc: String, val longDesc: String, val price: Double?)

object JProduct : JAny<Product>() {

    private val id by JField(Product::id, JInt)
    private val long_description by JField(Product::longDesc, JString)
    private val short_desc by JField(Product::shortDesc, JString)
    private val price by JFieldMaybe(Product::price, JDouble)

    override fun JsonNodeObject.deserializeOrThrow() =
        Product(
            id = +id,
            shortDesc = +short_desc,
            longDesc = +long_description,
            price = +price
        )
}


data class InvoiceId(override val raw: String) : StringWrapper

enum class TaxType {
    Domestic, Exempt, EU, US, Other
}


data class Invoice(
    val id: InvoiceId,
    val vat: Boolean,
    val customer: Person,
//    val customer: Customer,
    val items: List<Product>,
    val total: BigDecimal,
    val created: LocalDate,
    val paid: Instant?
)

object JCompany : JAny<Company>() {

    val name by JField(Company::name, JString)
    val tax_type by JField(Company::taxType, JEnum(TaxType::valueOf))

    override fun JsonNodeObject.deserializeOrThrow(): Company? =
        Company(
            name = +name,
            taxType = +tax_type
        )
}

object JCustomer : JSealed<Customer> {
    override val subtypesMap: Map<String, JObjectBase<out Customer>> =
        mapOf(
            "private" to JPerson,
            "company" to JCompany
        )

}

object JInvoice : JAny<Invoice>() {
    val id by JField(Invoice::id, JStringWrapper(::InvoiceId))
    val vat by JField(Invoice::vat, JBoolean, jsonFieldName = "vat-to-pay")
    val customer by JField(Invoice::customer, JPerson)
    val items by JField(Invoice::items, JArray(JProduct))
    val total by JField(Invoice::total, JBigDecimal)
    val created_date by JField(Invoice::created, JLocalDate)
    val paid_datetime by JFieldMaybe(Invoice::paid, JInstant)


    override fun JsonNodeObject.deserializeOrThrow(): Invoice =
        Invoice(
            id = +id,
            vat = +vat,
            customer = +customer,
            items = +items,
            total = +total,
            created = +created_date,
            paid = +paid_datetime
        )

}