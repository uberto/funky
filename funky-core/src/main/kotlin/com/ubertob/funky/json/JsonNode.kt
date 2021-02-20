package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.asFailure
import com.ubertob.funky.outcome.asSuccess
import com.ubertob.funky.outcome.onFailure

sealed class JsonNode {

    abstract val path: List<String>

    fun asText(): Outcome<JsonError, String> =
        when (this) {
            is JsonNodeString -> this.text.asSuccess()
            else -> JsonError(this, "Expected Text but node.type is ${this::class.simpleName}").asFailure()
        }

    fun asDouble(): Outcome<JsonError, Double> =
        when (this) {
            is JsonNodeDouble -> this.num.asSuccess()
            is JsonNodeLong -> this.num.toDouble().asSuccess()
            is JsonNodeInt -> this.num.toDouble().asSuccess()
            else -> JsonError(this, "Expected Double but found $this").asFailure()
        }

    fun asInt(): Outcome<JsonError, Int> =
        when (this) {
            is JsonNodeInt -> this.num.asSuccess()
            else -> JsonError(this, "Expected Int but found $this").asFailure()
        }

    fun asLong(): Outcome<JsonError, Long> =
        when (this) {
            is JsonNodeLong -> this.num.asSuccess()
            is JsonNodeInt -> this.num.toLong().asSuccess()
            else -> JsonError(this, "Expected Long but found $this").asFailure()
        }

    fun asBoolean(): Outcome<JsonError, Boolean> =
        when (this) {
            is JsonNodeBoolean -> this.value.asSuccess()
            else -> JsonError(this, "Expected Boolean but found $this").asFailure()
        }

    fun <T> asObject(f: (JsonNodeObject) -> Outcome<JsonError, T>): Outcome<JsonError, T> =
        when (this) {
            is JsonNodeObject -> f(this)
            else -> JsonError(this, "Expected Object but found $this").asFailure()
        }

    fun <JN : JsonNode> asArray(): Outcome<JsonError, List<JN>> =
        when (this) {
            is JsonNodeArray<*> -> (values as List<JN>).asSuccess()
            else -> JsonError(this, "Expected Array but found $this").asFailure()
        }

    fun asNull(): Outcome<JsonError, Any?> =
        when (this) {
            is JsonNodeNull -> null.asSuccess()
            else -> JsonError(this, "Expected Null but found $this").asFailure()
        }


}

data class JsonNodeArray<JN : JsonNode>(val values: List<JN>, override val path: List<String> = emptyList()) :
    JsonNode()

data class JsonNodeBoolean(val value: Boolean, override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeDouble(val num: Double, override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeInt(val num: Int, override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeLong(val num: Long, override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeNull(override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeString(val text: String, override val path: List<String> = emptyList()) : JsonNode()
data class JsonNodeObject(val fieldMap: Map<String, JsonNode>, override val path: List<String> = emptyList()) :
    JsonNode() {

    operator fun <T> JsonProperty<T>.unaryPlus(): T =
        getter(this@JsonNodeObject)
            .onFailure { throw JsonParsingException(it) }
}
