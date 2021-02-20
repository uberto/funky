package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.Outcome.Companion.tryThis

fun <T> tryParse(f: () -> T): Outcome<JsonError, T> = tryThis(f).transformFailure { JsonError(null, it.msg) }


fun <JN : JsonNode> parseJsonNodeArray(tokens: Sequence<String>): Outcome<JsonError, JsonNodeArray<JN>> = TODO()

fun parseJsonNodeBoolean(tokens: Sequence<String>): Outcome<JsonError, JsonNodeBoolean> = TODO()

fun parseJsonNodeDouble(tokens: Sequence<String>): Outcome<JsonError, JsonNodeDouble> = TODO()

fun parseJsonNodeInt(tokens: Sequence<String>): Outcome<JsonError, JsonNodeInt> =
    tryParse { JsonNodeInt(tokens.first().toInt()) }

fun parseJsonNodeLong(tokens: Sequence<String>): Outcome<JsonError, JsonNodeLong> = TODO()

fun parseJsonNodeNull(tokens: Sequence<String>): Outcome<JsonError, JsonNodeNull> = TODO()

fun parseJsonNodeString(tokens: Sequence<String>): Outcome<JsonError, JsonNodeString> = TODO()

fun parseJsonNodeObject(tokens: Sequence<String>): Outcome<JsonError, JsonNodeObject> = TODO()


fun JsonNode.render(): String =
    when (this) {
        is JsonNodeNull -> "null"
        is JsonNodeString -> "$text" //todo escaping
        is JsonNodeInt -> num.toString()
        is JsonNodeBoolean -> if (value) "true" else "false"
        is JsonNodeLong -> num.toString()
        is JsonNodeDouble -> num.toString()
        is JsonNodeArray<*> -> TODO()
        is JsonNodeObject -> TODO()
    }
