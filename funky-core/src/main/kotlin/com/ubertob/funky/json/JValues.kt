package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.bind
import com.ubertob.funky.outcome.sequence


object JBoolean : BiDiJson<Boolean, JsonNodeBoolean> {

    override fun fromJsonNode(node: JsonNodeBoolean): Outcome<JsonError, Boolean> = node.asBoolean()
    override fun toJsonNode(value: Boolean): JsonNodeBoolean = JsonNodeBoolean(value)
    override fun parseToNode(tokensStream: TokensStream): Outcome<JsonError, JsonNodeBoolean> =
        parseJsonNodeBoolean(tokensStream)

}

object JString : BiDiJson<String, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, String> = node.asText()
    override fun toJsonNode(value: String): JsonNodeString = JsonNodeString(value)
    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeString> =
        parseJsonNodeString(tokensStream)

}

object JInt : BiDiJson<Int, JsonNodeInt> {

    override fun fromJsonNode(node: JsonNodeInt): Outcome<JsonError, Int> = node.asInt()
    override fun toJsonNode(value: Int): JsonNodeInt = JsonNodeInt(value)
    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeInt> = parseJsonNodeInt(tokensStream)
}


object JLong : BiDiJson<Long, JsonNodeLong> {

    override fun fromJsonNode(node: JsonNodeLong): Outcome<JsonError, Long> = node.asLong()
    override fun toJsonNode(value: Long): JsonNodeLong = JsonNodeLong(value)
    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeLong> = parseJsonNodeLong(tokensStream)
}

object JDouble : BiDiJson<Double, JsonNodeDouble> {

    override fun fromJsonNode(node: JsonNodeDouble): Outcome<JsonError, Double> = node.asDouble()
    override fun toJsonNode(value: Double): JsonNodeDouble = JsonNodeDouble(value)
    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeDouble> =
        parseJsonNodeDouble(tokensStream)
}

data class JStringWrapper<T : StringWrapper>(val cons: (String) -> T) : BiDiJson<T, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, T> = node.asText().transform(cons)
    override fun toJsonNode(value: T): JsonNodeString = JsonNodeString(value.raw)
    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeString> =
        parseJsonNodeString(tokensStream)

}

data class JArray<T : Any, JN : JsonNode>(val helper: BiDiJson<T, JN>) : BiDiJson<List<T>, JsonNodeArray<JN>> {

    override fun fromJsonNode(node: JsonNodeArray<JN>): Outcome<JsonError, List<T>> =
        mapFrom(node, helper::fromJsonNode)

    override fun toJsonNode(value: List<T>): JsonNodeArray<JN> = mapToJson(value, helper::toJsonNode)

    private fun <T : Any> mapToJson(objs: List<T>, f: (T) -> JN): JsonNodeArray<JN> =
        JsonNodeArray(objs.map(f))

    private fun <T : Any> mapFrom(
        node: JsonNodeArray<JN>,
        f: (JN) -> Outcome<JsonError, T>
    ): Outcome<JsonError, List<T>> =
        node.asArray<JN>().bind { nodes -> nodes.map { n: JN -> f(n) }.sequence() }

    override fun parseToNode(tokensStream: TokensStream): JsonOutcome<JsonNodeArray<JN>> =
        parseJsonNodeArray(tokensStream, helper::parseToNode)
}
