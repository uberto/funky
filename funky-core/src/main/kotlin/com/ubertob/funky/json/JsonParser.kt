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
                else -> error("expected Boolean at pos ${tokens.position()} but found $it")
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
        if (openQuote != "\"") error("Expected quote at pos ${tokens.position()} but found $openQuote")
        if (endQuote != "\"") error("Expected quote at pos ${tokens.position()} but found $endQuote")
        JsonNodeString(text)
    }


typealias TokenStreamParser<T> = (TokensStream) -> JsonOutcome<T>


fun <JN : JsonNode> parseJsonNodeArray(
    tokens: TokensStream,
    tokenParser: TokenStreamParser<JN>
): JsonOutcome<JsonNodeArray<JN>> =
    tryParse {
        val openBraket = tokens.next()
        if (openBraket != "[") error("Expected open bracket at pos ${tokens.position()} but found $openBraket")
        else {
            var curr = tokens.peek()
            val nodes = mutableListOf<JN>()
            while (curr != "]") {
                nodes.add(tokenParser(tokens).orThrow())
                curr = tokens.peek()
                if (curr != "," && curr != "]") error("Expected comma or close bracket at pos ${tokens.position()} but found $curr")
                tokens.next()
            }
            JsonNodeArray(nodes)
        }
    }

fun parseJsonNodeObject(
    tokens: TokensStream,
    fieldParsers: Map<String, TokenStreamParser<JsonNode>>
): Outcome<JsonError, JsonNodeObject> =
    tryParse {
        val openCurly = tokens.next()
        if (openCurly != "{") error("Expected open curly at pos ${tokens.position()} but found: $openCurly")
        else {
            var curr = tokens.peek()
            val fields = mutableMapOf<String, JsonNode>()
            while (curr != "}") {
                val fieldName = parseJsonNodeString(tokens).orThrow().text

                val parser = fieldParsers.get(fieldName)
                    ?: error("Unexpected field $fieldName at pos ${tokens.position()} fields: ${fieldParsers.keys}")

                val colon = tokens.next()
                if (colon != ":") error("Expected colon but found: $colon")
                val value = parser(tokens).orThrow()
                fields.put(fieldName, value)

                curr = tokens.peek()
                if (curr != "," && curr != "}") error("Expected comma or close curly at pos ${tokens.position()} but found $curr")
                tokens.next()
            }
            JsonNodeObject(fields)
        }

    }


fun JsonNode.render(): String =
    when (this) {
        is JsonNodeNull -> "null"
        is JsonNodeString -> text.putInQuotes()
        is JsonNodeInt -> num.toString()
        is JsonNodeBoolean -> value.toString()
        is JsonNodeLong -> num.toString()
        is JsonNodeDouble -> num.toString()
        is JsonNodeArray<*> -> values.map { it.render() }.joinToString(prefix = "[", postfix = "]")
        is JsonNodeObject -> fieldMap.entries.map { it.key.putInQuotes() + ": " + it.value.render() }
            .joinToString(prefix = "{", postfix = "}")
    }

private fun String.putInQuotes(): String = replace("\"", "\\\"").let { "\"${it}\"" }
