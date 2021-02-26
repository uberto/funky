package com.ubertob.funky.json

import com.ubertob.funky.outcome.*


sealed class JsonNode {
    abstract val path: NodePath
}


data class JsonNodeBoolean(val value: Boolean, override val path: NodePath) : JsonNode()
data class JsonNodeDouble(val num: Double, override val path: NodePath) : JsonNode()
data class JsonNodeInt(val num: Int, override val path: NodePath) : JsonNode()
data class JsonNodeLong(val num: Long, override val path: NodePath) : JsonNode()
data class JsonNodeNull(override val path: NodePath) : JsonNode()
data class JsonNodeString(val text: String, override val path: NodePath) : JsonNode()
data class JsonNodeArray<JN : JsonNode>(val values: List<JN>, override val path: NodePath) : JsonNode()
data class JsonNodeObject(val fieldMap: Map<String, JsonNode>, override val path: NodePath) : JsonNode() {

    operator fun <T> JsonProperty<T>.unaryPlus(): T =
        getter(this@JsonNodeObject)
            .onFailure { throw JsonParsingException(it) }
}
