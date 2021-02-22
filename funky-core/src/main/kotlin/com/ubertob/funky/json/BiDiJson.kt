package com.ubertob.funky.json


import JsonLexer
import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.bind

interface StringWrapper {
    val raw: String
}

data class JsonError(val node: JsonNode?, val reason: String) : OutcomeError {
    val location = node?.path?.joinToString(separator = "/", prefix = "</", postfix = ">") ?: "parsing"
    override val msg = "error at $location: $reason"
}

typealias JsonOutcome<T> = Outcome<JsonError, T>

val defaultLexer = JsonLexer()

interface BiDiJson<T, JN : JsonNode> {
    fun fromJsonNode(node: JN): JsonOutcome<T>
    fun toJsonNode(value: T): JN

    fun parseToNode(tokensStream: TokensStream): JsonOutcome<JN>

    fun toJson(value: T): String = toJsonNode(value).render()
    fun fromJson(jsonString: String, lexer: JsonLexer = defaultLexer): JsonOutcome<T> =
        lexer.tokenize(jsonString)
            .let(this::parseToNode)
            .bind { fromJsonNode(it) }
}




