package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
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

    val typeName: String
        get() = "_type"

    val subtypesMap: Map<String, JObjectBase<out T>>

    override fun JsonNodeObject.deserializeOrThrow(): T? =
        subtypesMap[typeName]
            ?.fromJsonNode(this)
            ?.orThrow()
            ?: error("subtype not known $typeName")


    override fun toJsonNode(value: T, path: NodePath): JsonNodeObject =
        getWriters()
            .fold(JsonNodeObject(emptyMap(), path)) { acc, writer ->
                writer(acc, value)
            }

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): Outcome<JsonError, JsonNodeObject> =
        TODO("parseToNode")

    override fun getWriters(): Set<NodeWriter<T>> {
        TODO("Not yet implemented")
    }

    override fun getParsers(): Map<String, TokenStreamParser<JsonNode>> {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    fun <U : T> convertToNodeObj(conv: JObjectBase<out T>, subtype: String, aValue: U, path: NodePath): JsonNodeObject =
        (conv as? JObjectBase<U>
            ?: error("subtype $subtype does not match with $aValue")).toJsonNode(aValue, path)

    /*
    fun <U : T> serializeSubtype(subtype: String, aValue: U): JsonNodeObject {
        val conv = subtypesMap[subtype] ?: throw UnknownSubtypeException("subtype not known $subtype")
        val jsonNodeObject = convertToNodeObj(conv, subtype, aValue)
        return writeObjNode(typeName to JString.build(subtype), *jsonNodeObject.fieldMap.toList().toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    fun <U : T> convertToNodeObj(conv: JProtocol<out T>, subtype: String, aValue: U): JsonNodeObject =
        (conv as? JProtocol<U>
            ?: throw UnknownSubtypeException("subtype $subtype does not match with $aValue")).serialize(aValue)

    override fun deserialize1(from: JsonNodeObject): Outcome<JsonError, T> =
        JString.getFieldFromNode(from, typeName).flatMap { subtype ->
            subtypesMap.get(subtype)
                ?.deserialize1(from)
                ?: JsonError(from, "subtype not known $subtype").asFailure()
        }

     */
}

class JMap<T : Any>(private val valueConverter: JObjectBase<T>) : JObjectBase<Map<String, T>> {
    override fun JsonNodeObject.deserializeOrThrow(): Map<String, T>? {
        TODO("Not yet implemented")
    }

    override fun getWriters(): Set<NodeWriter<Map<String, T>>> {
        TODO("Not yet implemented")
    }

    override fun getParsers(): Map<String, TokenStreamParser<JsonNode>> {
        TODO("Not yet implemented")
    }

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeObject> {
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

