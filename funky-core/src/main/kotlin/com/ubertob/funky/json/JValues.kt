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


data class JArray<T : Any, JN : JsonNode>(val helper: BiDiJson<T, JN>) : BiDiJson<List<T>, JsonNodeArray<JN>> {

    override fun fromJsonNode(node: JsonNodeArray<JN>): Outcome<JsonError, List<T>> =
        mapFrom(node, helper::fromJsonNode)

    override fun toJsonNode(value: List<T>, path: NodePath): JsonNodeArray<JN> =
        mapToJson(value, helper::toJsonNode, path)

    private fun <T : Any> mapToJson(objs: List<T>, f: (T, NodePath) -> JN, path: NodePath): JsonNodeArray<JN> =
        JsonNodeArray(objs.map { f(it, path) }, path)

    private fun <T : Any> mapFrom(
        node: JsonNodeArray<JN>,
        f: (JN) -> JsonOutcome<T>
    ): JsonOutcome<List<T>> = node.values.map(f).extract()

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeArray<JN>> =
        parseJsonNodeArray(tokensStream, helper::parseToNode, path)
}
