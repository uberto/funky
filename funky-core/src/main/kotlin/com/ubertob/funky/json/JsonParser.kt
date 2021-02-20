package com.ubertob.funky.json


fun <JN : JsonNode> parseJsonNodeArray(jsonString: String): JsonNodeArray<JN> = TODO()

fun parseJsonNodeBoolean(jsonString: String): JsonNodeBoolean = TODO()

fun parseJsonNodeDouble(jsonString: String): JsonNodeDouble = TODO()

fun parseJsonNodeInt(jsonString: String): JsonNodeInt = JsonNodeInt(jsonString.toInt())//put outcome around

fun parseJsonNodeLong(jsonString: String): JsonNodeLong = TODO()

fun parseJsonNodeNull(jsonString: String): JsonNodeNull = TODO()

fun parseJsonNodeString(jsonString: String): JsonNodeString = TODO()

fun parseJsonNodeObject(jsonString: String): JsonNodeObject = TODO()


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
