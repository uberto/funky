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

object JInstantD : JStringRepresentable<Instant>() {
    override val cons: (String) -> Instant = Instant::parse
    override val render: (Instant) -> String = Instant::toString
}

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


interface JSealed<T : Any> : JObjectBase<T> {

    val typeFieldName: String
        get() = "_type"

    fun typeWriter(jno: JsonNodeObject, obj: T): JsonNodeObject =
        jno.copy(
            fieldMap = jno.fieldMap + (typeFieldName to JsonNodeString(
                extractTypeName(obj),
                Node(typeFieldName, jno.path)
            ))
        )

    fun extractTypeName(obj: T): String

    val subtypesMap: Map<String, JObjectBase<out T>>

    override fun JsonNodeObject.deserializeOrThrow(): T? {
        val typeName: JsonNodeString =
            fieldMap[typeFieldName] as? JsonNodeString ?: error("expected field $typeFieldName not found!")
        val bidiJson = subtypesMap[typeName.text] ?: error("subtype not known $typeName")
        return bidiJson.fromJsonNode(this).orThrow()
    }


    @Suppress("UNCHECKED_CAST")
    override fun getWriters(value: T): Sequence<NodeWriter<T>> = sequence {
        val typeName = extractTypeName(value)
        yield(::typeWriter)
        yieldAll(subtypesMap[typeName]?.let { (it as JObjectBase<T>).getWriters(value) }
            ?: error("subtype not known $typeName"))
    }
}

class JMap<T : Any>(private val valueConverter: JObjectBase<T>) : JObjectBase<Map<String, T>> {
    override fun JsonNodeObject.deserializeOrThrow(): Map<String, T>? {
        TODO("Not yet implemented")
    }


    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeObject> {
        TODO("Not yet implemented")
    }

    override fun getWriters(value: Map<String, T>): Sequence<NodeWriter<Map<String, T>>> {
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

