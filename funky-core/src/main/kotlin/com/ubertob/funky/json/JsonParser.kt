package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.Outcome.Companion.tryThis

fun <T> tryParse(f: () -> T): Outcome<JsonError, T> = tryThis(f).transformFailure { JsonError(null, it.msg) }

typealias TokensStream = PeekingIterator<String>

fun parseJsonNodeBoolean(tokens: TokensStream): Outcome<JsonError, JsonNodeBoolean> =
    tryParse {
        tokens.next().let {
            when (it) {
                "true" -> true
                "false" -> false
                else -> error("expected Boolean! $it")
            }.let { JsonNodeBoolean(it) }
        }
    }

fun parseJsonNodeDouble(tokens: TokensStream): Outcome<JsonError, JsonNodeDouble> =
    tryParse { JsonNodeDouble(tokens.next().toDouble()) }


fun parseJsonNodeInt(tokens: TokensStream): Outcome<JsonError, JsonNodeInt> =
    tryParse { JsonNodeInt(tokens.next().toInt()) }

fun parseJsonNodeLong(tokens: TokensStream): Outcome<JsonError, JsonNodeLong> =
    tryParse { JsonNodeLong(tokens.next().toLong()) }

fun parseJsonNodeNull(tokens: TokensStream): Outcome<JsonError, JsonNodeNull> =
    tryParse { tokens.next().let { if (it == "null") JsonNodeNull() else error("Expected null! $it") } }

fun parseJsonNodeString(tokens: TokensStream): Outcome<JsonError, JsonNodeString> =
    tryParse {
        val openQuote = tokens.next()
        val text = tokens.next()
        val endQuote = tokens.next()
        if (openQuote != "\"") error("Expected quote! $openQuote")
        if (endQuote != "\"") error("Expected quote! $endQuote")
        JsonNodeString(text)
    }


fun <JN : JsonNode> parseJsonNodeArray(
    tokens: TokensStream,
    tokenParser: (TokensStream) -> JN
): Outcome<JsonError, JsonNodeArray<JN>> =
    tryParse {
        val openBraket = tokens.next()

        if (openBraket != "[") error("Expected open bracket! $openBraket")
        else {
            var curr = tokens.peek()
            val nodes = mutableListOf<JN>()
            while (curr != "]") {
                nodes.add(tokenParser(tokens))
                curr = tokens.peek()
                if (curr != "," && curr != "]") error("Expected comma or close bracket! $curr")
                tokens.next()
            }
            JsonNodeArray(nodes)
        }
    }

fun parseJsonNodeObject(tokens: TokensStream): Outcome<JsonError, JsonNodeObject> = TODO()


fun JsonNode.render(): String =
    when (this) {
        is JsonNodeNull -> "null"
        is JsonNodeString -> text.replace("\"", "\\\"").let { "\"${it}\"" }
        is JsonNodeInt -> num.toString()
        is JsonNodeBoolean -> value.toString()
        is JsonNodeLong -> num.toString()
        is JsonNodeDouble -> num.toString()
        is JsonNodeArray<*> -> values.map { it.render() }.joinToString(prefix = "[", postfix = "]")
        is JsonNodeObject -> TODO()
    }
