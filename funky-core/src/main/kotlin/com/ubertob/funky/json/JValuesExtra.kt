package com.ubertob.funky.json

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.util.*

interface StringWrapper {
    val raw: String
}


data class JStringWrapper<T : StringWrapper>(override val cons: (String) -> T) : JStringRepresentable<T>() {
    override val render: (T) -> String = { it.raw }
}

object JBigDecimal : JNumRepresentable<BigDecimal>() {
    override val cons: (BigDecimal) -> BigDecimal = { it }
    override val render: (BigDecimal) -> BigDecimal = { it }
}

object JBigInteger : JNumRepresentable<BigInteger>() {
    override val cons: (BigDecimal) -> BigInteger = BigDecimal::toBigInteger
    override val render: (BigInteger) -> BigDecimal = BigInteger::toBigDecimal
}


object JCurrency : JStringRepresentable<Currency>() {
    override val cons: (String) -> Currency = Currency::getInstance
    override val render: (Currency) -> String = Currency::getCurrencyCode
}

data class JEnum<E : Enum<E>>(override val cons: (String) -> E) : JStringRepresentable<E>() {
    override val render: (E) -> String = { it.name }
}

//instant as date string
object JInstantD : JStringRepresentable<Instant>() {
    override val cons: (String) -> Instant = Instant::parse
    override val render: (Instant) -> String = Instant::toString
}

//instant as epoch millis
object JInstant : JNumRepresentable<Instant>() {
    override val cons: (BigDecimal) -> Instant = { Instant.ofEpochMilli(it.toLong()) }
    override val render: (Instant) -> BigDecimal = { it.toEpochMilli().toBigDecimal() }
}

object JLocalDate : JStringRepresentable<LocalDate>() {
    override val cons: (String) -> LocalDate = LocalDate::parse
    override val render: (LocalDate) -> String = LocalDate::toString
}

//for serializing Kotlin object and other single instance types
data class JInstance<T : Any>(val singleton: T) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = singleton
}


interface JSealed<T : Any> : JObjectBiDi<T> {

    val typeFieldName: String
        get() = "_type"


    fun extractTypeName(obj: T): String

    val subtypeBiDis: Map<String, JObjectBiDi<out T>>

    fun typeWriter(jno: JsonNodeObject, obj: T): JsonNodeObject =
        jno.copy(
            fieldMap = jno.fieldMap + (typeFieldName to JsonNodeString(
                extractTypeName(obj),
                Node(typeFieldName, jno.path)
            ))
        )

    override fun JsonNodeObject.deserializeOrThrow(): T? {
        val typeName = JString.fromJsonNodeBase(
            fieldMap[typeFieldName]
                ?: error("expected field $typeFieldName not found!")
        ).orThrow()
        val bidiJson = subtypeBiDis[typeName] ?: error("subtype not known $typeName")
        return bidiJson.fromJsonNode(this).orThrow()
    }


    override fun getWriters(value: T): Set<NodeWriter<T>> =
        extractTypeName(value).let { typeName ->
            findSubTypeBiDi(typeName)
                ?.getWriters(value)
                ?.plus(::typeWriter)
                ?: error("subtype not known $typeName")
        }

    @Suppress("UNCHECKED_CAST")
    fun findSubTypeBiDi(typeName: String): JObjectBiDi<T>? = subtypeBiDis.get(typeName) as? JObjectBiDi<T>

}

class JMap<T : Any>(private val valueConverter: JObjectBiDi<T>) : JObjectBiDi<Map<String, T>> {
    override fun JsonNodeObject.deserializeOrThrow(): Map<String, T>? {
        TODO("Not yet implemented")
    }


    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeObject> {
        TODO("Not yet implemented")
    }

    override fun getWriters(value: Map<String, T>): Set<NodeWriter<Map<String, T>>> {
        TODO("Not yet implemented")
    }
//    override fun extract(wrapped: JsonNodeObject): JsonOutcome<Map<String, T>> =
//        wrapped.asObject {
//            fieldMap.mapValues { entry ->
//                valueConverter.extract(entry.value)
//                    .onFailure { return@asObject it }
//            }.asSuccess()
//        }
//
//    override fun build(value: Map<String, T>): JsonNodeObject =
//        JsonNodeObject(value.mapValues { valueConverter.build(it.value) })
}

