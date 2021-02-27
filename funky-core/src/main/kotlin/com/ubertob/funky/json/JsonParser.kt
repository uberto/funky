package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.Outcome.Companion.tryThis
import com.ubertob.funky.outcome.asFailure
import com.ubertob.funky.outcome.onFailure
import java.math.BigDecimal

inline fun <T> tryParse(f: () -> T): Outcome<JsonError, T> = tryThis(f).transformFailure { JsonError(null, it.msg) }

typealias TokensStream = PeekingIterator<String>

fun parsingFailure(expected: String, position: Int, actual: String, path: NodePath) =
    JsonError(
        null,
        "Expected $expected at position $position but found '$actual' while parsing <${path.getPath()}>"
    ).asFailure()


fun parseJsonNodeBoolean(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeBoolean> =
    tryParse {
        tokens.next().let {
            when (it) {
                "true" -> true
                "false" -> false
                else -> return parsingFailure("a Boolean", tokens.position(), it, path)
            }.let { JsonNodeBoolean(it, path) }
        }
    }

fun parseJsonNodeNum(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeNum> =
    tryParse {
        JsonNodeNum(BigDecimal(tokens.next()), path)
    }


fun parseJsonNodeNull(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeNull> =
    tryParse {
        tokens.next()
            .let {
                if (it == "null") JsonNodeNull(path) else
                    return parsingFailure("null", tokens.position(), it, path)
            }
    }

fun parseJsonNodeString(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeString> =
    tryParse {
        val openQuote = tokens.next()
        val text = tokens.next()
        val endQuote = tokens.next()
        if (openQuote != "\"") return parsingFailure("'\"'", tokens.position(), openQuote, path)
        if (endQuote != "\"") return parsingFailure("'\"'", tokens.position(), endQuote, path)
        JsonNodeString(text, path)
    }


typealias TokenStreamParser<T> = (TokensStream, NodePath) -> JsonOutcome<T>


fun parseJsonNodeArray(
    tokens: TokensStream,
    path: NodePath
): JsonOutcome<JsonNodeArray> =
    tryParse {
        val openBraket = tokens.next()
        if (openBraket != "[") return parsingFailure("'['", tokens.position(), openBraket, path)
        else {
            var currToken = tokens.peek()
            val nodes = mutableListOf<JsonNode>()
            var currNode = 0
            while (currToken != "]") {
                nodes.add(
                    parseNewNode(tokens, Node("${currNode++}", path)).onFailure { return it.asFailure() })
                currToken = tokens.peek()
                if (currToken != "," && currToken != "]") return parsingFailure(
                    "',' or ':'",
                    tokens.position(),
                    currToken,
                    path
                )
                tokens.next()
            }
            JsonNodeArray(nodes, path)
        }
    }

fun parseJsonNodeObject(
    tokens: TokensStream,
    path: NodePath   //todo add more context to NodePath? like the field type, expected values...
): Outcome<JsonError, JsonNodeObject> =
    tryParse {
        val openCurly = tokens.next()
        if (openCurly != "{") return parsingFailure("'{'", tokens.position(), openCurly, path)
        else {
            var curr = tokens.peek()
            val fields = mutableMapOf<String, JsonNode>()
            while (curr != "}") {
                val fieldName = parseJsonNodeString(tokens, path).onFailure { return it.asFailure() }.text
//
//                val parser = fieldParsers.get(fieldName)
//                    ?: return parsingFailure("one of ${fieldParsers.keys}", tokens.position(), fieldName, path)

                val colon = tokens.next()
                if (colon != ":") return parsingFailure("':'", tokens.position(), colon, path)
                val value = parseNewNode(tokens, Node(fieldName, path)).onFailure { return it.asFailure() }
                fields.put(fieldName, value)

                curr = tokens.peek()
                if (curr != "," && curr != "}") return parsingFailure("'}' or ','", tokens.position(), curr, path)
                tokens.next()
            }
            JsonNodeObject(fields, path)
        }

    }

fun parseNewNode(tokens: TokensStream, path: NodePath): JsonOutcome<JsonNode> =
    when (val first = tokens.peek()) {
        "null" -> parseJsonNodeNull(tokens, path)
        "false", "true" -> parseJsonNodeBoolean(tokens, path)
        "\"" -> parseJsonNodeString(tokens, path)
        "[" -> parseJsonNodeArray(tokens, path)
        "{" -> parseJsonNodeObject(tokens, path)
        else ->
            when (first.get(0)) { //regex -?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?
                in '0'..'9', '-' -> parseJsonNodeNum(tokens, path)
                else -> parsingFailure("a valid json value", tokens.position(), first, path)
            }
    }


fun JsonNode.render(): String =
    when (this) {
        is JsonNodeNull -> "null"
        is JsonNodeString -> text.putInQuotes()
        is JsonNodeBoolean -> value.toString()
        is JsonNodeNum -> num.toString()
        is JsonNodeArray -> values.map { it.render() }.joinToString(prefix = "[", postfix = "]")
        is JsonNodeObject -> fieldMap.entries.map { it.key.putInQuotes() + ": " + it.value.render() }
            .joinToString(prefix = "{", postfix = "}")
    }

private fun String.putInQuotes(): String = replace("\"", "\\\"").let { "\"${it}\"" }
