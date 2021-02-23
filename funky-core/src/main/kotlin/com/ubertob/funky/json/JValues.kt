package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome


object JBoolean : BiDiJson<Boolean, JsonNodeBoolean> {

    override fun fromJsonNode(node: JsonNodeBoolean): Outcome<JsonError, Boolean> = node.asBoolean()
    override fun toJsonNode(value: Boolean, path: NodePath): JsonNodeBoolean = JsonNodeBoolean(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): Outcome<JsonError, JsonNodeBoolean> =
        parseJsonNodeBoolean(tokensStream, path)

}

object JString : BiDiJson<String, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, String> = node.asText()
    override fun toJsonNode(value: String, path: NodePath): JsonNodeString = JsonNodeString(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeString> =
        parseJsonNodeString(tokensStream, path)

}

object JInt : BiDiJson<Int, JsonNodeInt> {

    override fun fromJsonNode(node: JsonNodeInt): Outcome<JsonError, Int> = node.asInt()
    override fun toJsonNode(value: Int, path: NodePath): JsonNodeInt = JsonNodeInt(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeInt> =
        parseJsonNodeInt(tokensStream, path)
}


object JLong : BiDiJson<Long, JsonNodeLong> {

    override fun fromJsonNode(node: JsonNodeLong): Outcome<JsonError, Long> = node.asLong()
    override fun toJsonNode(value: Long, path: NodePath): JsonNodeLong = JsonNodeLong(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeLong> =
        parseJsonNodeLong(tokensStream, path)
}

object JDouble : BiDiJson<Double, JsonNodeDouble> {

    override fun fromJsonNode(node: JsonNodeDouble): Outcome<JsonError, Double> = node.asDouble()
    override fun toJsonNode(value: Double, path: NodePath): JsonNodeDouble = JsonNodeDouble(value, path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeDouble> =
        parseJsonNodeDouble(tokensStream, path)
}

data class JStringWrapper<T : StringWrapper>(val cons: (String) -> T) : BiDiJson<T, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, T> = node.asText().transform(cons)
    override fun toJsonNode(value: T, path: NodePath): JsonNodeString = JsonNodeString(value.raw, path)
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
        f: (JN) -> Outcome<JsonError, T>
    ): Outcome<JsonError, List<T>> =
        node.asArray(f)

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeArray<JN>> =
        parseJsonNodeArray(tokensStream, helper::parseToNode, path)
}
