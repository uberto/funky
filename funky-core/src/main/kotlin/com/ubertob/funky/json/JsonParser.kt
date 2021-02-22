package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.Outcome.Companion.tryThis
import com.ubertob.funky.outcome.asFailure
import com.ubertob.funky.outcome.onFailure

inline fun <T> tryParse(f: () -> T): Outcome<JsonError, T> = tryThis(f).transformFailure { JsonError(null, it.msg) }

typealias TokensStream = PeekingIterator<String>

fun parsingFailure(expected: String, position: Int, actual: String) =
    JsonError(null, "Expected $expected at position $position but found '$actual'").asFailure()


fun parseJsonNodeBoolean(tokens: TokensStream): Outcome<JsonError, JsonNodeBoolean> =
    tryParse {
        tokens.next().let {
            when (it) {
                "true" -> true
                "false" -> false
                else -> return parsingFailure("a Boolean", tokens.position(), it)
            }.let { JsonNodeBoolean(it) }
        }
    }

fun parseJsonNodeDouble(tokens: TokensStream): Outcome<JsonError, JsonNodeDouble> =
    tryParse {
        JsonNodeDouble(tokens.next().let {
            it.toDoubleOrNull()
                ?: return parsingFailure("a Double", tokens.position(), it)
        })
    }


fun parseJsonNodeInt(tokens: TokensStream): Outcome<JsonError, JsonNodeInt> =
    tryParse {
        JsonNodeInt(tokens.next().let {
            it.toIntOrNull()
                ?: return parsingFailure("an Int", tokens.position(), it)
        })
    }

fun parseJsonNodeLong(tokens: TokensStream): Outcome<JsonError, JsonNodeLong> =
    tryParse {
        JsonNodeLong(tokens.next().let {
            it.toLongOrNull()
                ?: return parsingFailure("a Long", tokens.position(), it)
        })
    }

fun parseJsonNodeNull(tokens: TokensStream): Outcome<JsonError, JsonNodeNull> =
    tryParse {
        tokens.next()
            .let {
                if (it == "null") JsonNodeNull() else
                    return parsingFailure("null", tokens.position(), it)
            }
    }

fun parseJsonNodeString(tokens: TokensStream): Outcome<JsonError, JsonNodeString> =
    tryParse {
        val openQuote = tokens.next()
        val text = tokens.next()
        val endQuote = tokens.next()
        if (openQuote != "\"") return parsingFailure("'\"'", tokens.position(), openQuote)
        if (endQuote != "\"") return parsingFailure("'\"'", tokens.position(), endQuote)
        JsonNodeString(text)
    }


typealias TokenStreamParser<T> = (TokensStream) -> JsonOutcome<T>


fun <JN : JsonNode> parseJsonNodeArray(
    tokens: TokensStream,
    tokenParser: TokenStreamParser<JN>
): JsonOutcome<JsonNodeArray<JN>> =
    tryParse {
        val openBraket = tokens.next()
        if (openBraket != "[") return parsingFailure("'['", tokens.position(), openBraket)
        else {
            var currToken = tokens.peek()
            val nodes = mutableListOf<JN>()
            while (currToken != "]") {
                nodes.add(tokenParser(tokens).onFailure { return it.asFailure() })
                currToken = tokens.peek()
                if (currToken != "," && currToken != "]") return parsingFailure(
                    "',' or ':'",
                    tokens.position(),
                    currToken
                )
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
        if (openCurly != "{") return parsingFailure("'{'", tokens.position(), openCurly)
        else {
            var curr = tokens.peek()
            val fields = mutableMapOf<String, JsonNode>()
            while (curr != "}") {
                val fieldName = parseJsonNodeString(tokens).onFailure { return it.asFailure() }.text

                val parser = fieldParsers.get(fieldName)
                    ?: error("Unexpected field '$fieldName' at pos ${tokens.position()} possible fields: ${fieldParsers.keys}")

                val colon = tokens.next()
                if (colon != ":") return parsingFailure("':'", tokens.position(), colon)
                val value = parser(tokens).onFailure { return it.asFailure() }
                fields.put(fieldName, value)

                curr = tokens.peek()
                if (curr != "," && curr != "}") return parsingFailure("'}' or ','", tokens.position(), curr)
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
