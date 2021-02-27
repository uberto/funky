package com.ubertob.funky.json


import JsonLexer
import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.bind


data class JsonError(val node: JsonNode?, val reason: String) : OutcomeError {
    val location = node?.path?.getPath()?.let { "<$it>" } ?: "parsing"
    override val msg = "error at $location: $reason"
}

typealias JsonOutcome<T> = Outcome<JsonError, T>

val defaultLexer = JsonLexer()

interface BiDiJson<T, JN : JsonNode> {
    fun fromJsonNode(node: JN): JsonOutcome<T>
    fun toJsonNode(value: T, path: NodePath): JN

    fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JN>

    fun toJson(value: T): String = toJsonNode(value, NodeRoot).render()
    fun fromJson(jsonString: String, lexer: JsonLexer = defaultLexer): JsonOutcome<T> =
        parseToNode(lexer.tokenize(jsonString), NodeRoot)
            .bind { fromJsonNode(it) }
}




