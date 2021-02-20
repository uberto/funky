package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.Outcome.Companion.tryThis

fun <T> tryParse(f: () -> T): Outcome<JsonError, T> = tryThis(f).transformFailure { JsonError(null, it.msg) }


fun parseJsonNodeBoolean(tokens: Sequence<String>): Outcome<JsonError, JsonNodeBoolean> =
    tryParse {
        tokens.first().let {
            when (it) {
                "true" -> true
                "false" -> false
                else -> error("expected Boolean! $it")
            }.let { JsonNodeBoolean(it) }
        }
    }

fun parseJsonNodeDouble(tokens: Sequence<String>): Outcome<JsonError, JsonNodeDouble> =
    tryParse { JsonNodeDouble(tokens.first().toDouble()) }


fun parseJsonNodeInt(tokens: Sequence<String>): Outcome<JsonError, JsonNodeInt> =
    tryParse { JsonNodeInt(tokens.first().toInt()) }

fun parseJsonNodeLong(tokens: Sequence<String>): Outcome<JsonError, JsonNodeLong> =
    tryParse { JsonNodeLong(tokens.first().toLong()) }

fun parseJsonNodeNull(tokens: Sequence<String>): Outcome<JsonError, JsonNodeNull> =
    tryParse { tokens.first().let { if (it == "null") JsonNodeNull() else error("Expected null! $it") } }

fun parseJsonNodeString(tokens: Sequence<String>): Outcome<JsonError, JsonNodeString> =
    tryParse {
        val (openQuote, text, endQuote) = tokens.take(3).toList()
        if (openQuote != "\"") error("Expected quote! $openQuote")
        if (endQuote != "\"") error("Expected quote! $endQuote")
        JsonNodeString(text)
    }

fun <JN : JsonNode> parseJsonNodeArray(tokens: Sequence<String>): Outcome<JsonError, JsonNodeArray<JN>> = TODO()

fun parseJsonNodeObject(tokens: Sequence<String>): Outcome<JsonError, JsonNodeObject> = TODO()


fun JsonNode.render(): String =
    when (this) {
        is JsonNodeNull -> "null"
        is JsonNodeString -> text.replace("\"", "\\\"").let { "\"${it}\"" }
        is JsonNodeInt -> num.toString()
        is JsonNodeBoolean -> value.toString()
        is JsonNodeLong -> num.toString()
        is JsonNodeDouble -> num.toString()
        is JsonNodeArray<*> -> TODO()
        is JsonNodeObject -> TODO()
    }
