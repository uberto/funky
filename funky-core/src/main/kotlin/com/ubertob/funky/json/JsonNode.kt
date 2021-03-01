package com.ubertob.funky.json

import com.ubertob.funky.outcome.*
import java.math.BigDecimal

sealed class NodeKind<JN : JsonNode>(
    val desc: String,
    val parse: (tokensStream: TokensStream, path: NodePath) -> JsonOutcome<JN>
)

object NullNode : NodeKind<JsonNodeNull>("Null", ::parseJsonNodeNull)

object BooleanNode : NodeKind<JsonNodeBoolean>("Boolean", ::parseJsonNodeBoolean)

object NumberNode : NodeKind<JsonNodeNumber>("Number", ::parseJsonNodeNum)

object StringNode : NodeKind<JsonNodeString>("String", ::parseJsonNodeString)

object ArrayNode : NodeKind<JsonNodeArray>("Array", ::parseJsonNodeArray)

object ObjectNode : NodeKind<JsonNodeObject>("Object", ::parseJsonNodeObject)

sealed class JsonNode {
    abstract val path: NodePath

    fun nodeKind(): NodeKind<*> =
        when (this) {
            is JsonNodeArray -> ArrayNode
            is JsonNodeBoolean -> BooleanNode
            is JsonNodeNull -> NullNode
            is JsonNodeNumber -> NumberNode
            is JsonNodeObject -> ObjectNode
            is JsonNodeString -> StringNode
        }
}


data class JsonNodeNull(override val path: NodePath) : JsonNode()
data class JsonNodeBoolean(val value: Boolean, override val path: NodePath) : JsonNode()
data class JsonNodeNumber(val num: BigDecimal, override val path: NodePath) : JsonNode()
data class JsonNodeString(val text: String, override val path: NodePath) : JsonNode()
data class JsonNodeArray(val values: List<JsonNode>, override val path: NodePath) : JsonNode()
data class JsonNodeObject(val fieldMap: Map<String, JsonNode>, override val path: NodePath) : JsonNode() {

    operator fun <T> JsonProperty<T>.unaryPlus(): T =
        getter(this@JsonNodeObject)
            .onFailure { throw JsonParsingException(it) }

}
