package com.ubertob.funky.json


import JsonLexer
import com.ubertob.funky.outcome.*


data class JsonError(val node: JsonNode?, val reason: String) : OutcomeError {
    val location = node?.path?.getPath()?.let { "<$it>" } ?: "parsing"
    override val msg = "error at $location: $reason"
}

typealias JsonOutcome<T> = Outcome<JsonError, T>

/*
a couple parser/printer form an adjunction

The laws are (no id because we cannot reconstruct a wrong json from the error):

render `.` parse `.` render = render
parse `.` render `.` parse = parse

where:
f `.` g: (x) -> g(f(x))
render : JsonOutcome<T> -> JSON
parse : JSON -> JsonOutcome<T>

JSON here can be either the Json string or the JsonNode
 */

interface JsonAdjunction<T, JN : JsonNode> {

    @Suppress("UNCHECKED_CAST")
    fun fromJsonNodeBase(node: JsonNode): JsonOutcome<T> =
        (node as? JN)?.let { fromJsonNode(it) } ?: JsonError(node, "Wrong JsonNode type!").asFailure()

    fun fromJsonNode(node: JN): JsonOutcome<T>
    fun toJsonNode(value: T, path: NodePath): JN

    fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JN>

    fun toJson(value: T): String = toJsonNode(value, NodeRoot).render()
    fun fromJson(jsonString: String): JsonOutcome<T> {
        val tokensStream = JsonLexer(jsonString).tokenize()
        return parseToNode(tokensStream, NodeRoot)
            .bind { fromJsonNode(it) }
            .bind {
                if (tokensStream.hasNext())
                    parsingFailure("EOF", tokensStream.next(), tokensStream.position(), NodeRoot)
                else
                    it.asSuccess()
            }
    }
}




