package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.asSuccess
import com.ubertob.funky.outcome.extract
import java.math.BigDecimal


object JBoolean : BiDiJson<Boolean, JsonNodeBoolean> {

    override fun fromJsonNode(node: JsonNodeBoolean): JsonOutcome<Boolean> = node.value.asSuccess()
    override fun toJsonNode(value: Boolean, path: NodePath): JsonNodeBoolean = JsonNodeBoolean(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeBoolean> =
        parseJsonNodeBoolean(tokensStream, path)

}

object JString : BiDiJson<String, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): JsonOutcome<String> = node.text.asSuccess()
    override fun toJsonNode(value: String, path: NodePath): JsonNodeString = JsonNodeString(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeString> =
        parseJsonNodeString(tokensStream, path)

}

object JDouble : JNumRepresentable<Double>() {

    override val cons: (BigDecimal) -> Double = BigDecimal::toDouble
    override val render: (Double) -> BigDecimal = Double::toBigDecimal
}


object JInt : JNumRepresentable<Int>() {
    override val cons: (BigDecimal) -> Int = BigDecimal::toInt
    override val render: (Int) -> BigDecimal = Int::toBigDecimal
}

object JLong : JNumRepresentable<Long>() {
    override val cons: (BigDecimal) -> Long = BigDecimal::toLong
    override val render: (Long) -> BigDecimal = Long::toBigDecimal
}

fun <T : Any> tryFromNode(node: JsonNode, f: () -> T): JsonOutcome<T> =
    Outcome.tryThis {
        f()
    }.transformFailure { throwableError ->
        when (throwableError.t) {
            is JsonParsingException -> throwableError.t.error // keep path info
            else -> JsonError(node, throwableError.msg)
        }
    }

abstract class JNumRepresentable<T : Any>() : BiDiJson<T, JsonNodeNum> {
    abstract val cons: (BigDecimal) -> T
    abstract val render: (T) -> BigDecimal

    override fun fromJsonNode(node: JsonNodeNum): JsonOutcome<T> = tryFromNode(node) { cons(node.num) }
    override fun toJsonNode(value: T, path: NodePath): JsonNodeNum = JsonNodeNum(render(value), path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeNum> =
        parseJsonNodeNum(tokensStream, path)
}

abstract class JStringRepresentable<T : Any>() : BiDiJson<T, JsonNodeString> {
    abstract val cons: (String) -> T
    abstract val render: (T) -> String

    override fun fromJsonNode(node: JsonNodeString): JsonOutcome<T> = tryFromNode(node) { cons(node.text) }
    override fun toJsonNode(value: T, path: NodePath): JsonNodeString = JsonNodeString(render(value), path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeString> =
        JString.parseToNode(tokensStream, path)
}


data class JArray<T : Any>(val helper: BiDiJson<T, *>) : BiDiJson<List<T>, JsonNodeArray> {

    override fun fromJsonNode(node: JsonNodeArray): Outcome<JsonError, List<T>> =
        mapFrom(node) { jn -> helper.fromJsonNodeBase(jn) }

    override fun toJsonNode(value: List<T>, path: NodePath): JsonNodeArray =
        mapToJson(value, helper::toJsonNode, path)

    private fun <T : Any> mapToJson(objs: List<T>, f: (T, NodePath) -> JsonNode, path: NodePath): JsonNodeArray =
        JsonNodeArray(objs.map { f(it, path) }, path)

    private fun <T : Any> mapFrom(
        node: JsonNodeArray,
        f: (JsonNode) -> JsonOutcome<T>
    ): JsonOutcome<List<T>> = node.values.map(f).extract()

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeArray> =
        parseJsonNodeArray(tokensStream, path)
}
