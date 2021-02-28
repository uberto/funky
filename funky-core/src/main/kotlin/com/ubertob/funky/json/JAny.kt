package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.asFailure
import com.ubertob.funky.outcome.asSuccess
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


typealias NodeWriter<T> = (JsonNodeObject, T) -> JsonNodeObject

interface JObjectBiDi<T : Any> : BiDiJson<T, JsonNodeObject> {

    fun JsonNodeObject.deserializeOrThrow(): T?

    override fun fromJsonNode(node: JsonNodeObject): JsonOutcome<T> =
        tryFromNode(node) {
            node.deserializeOrThrow() ?: throw JsonParsingException(
                JsonError(node, "tryDeserialize returned null!")
            )
        }

    fun getWriters(value: T): Set<NodeWriter<T>>

    override fun toJsonNode(value: T, path: NodePath): JsonNodeObject =
        getWriters(value)
            .fold(JsonNodeObject(emptyMap(), path)) { acc, writer ->
                writer(acc, value)
            }

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): Outcome<JsonError, JsonNodeObject> =
        parseJsonNodeObject(tokensStream, path)

}


abstract class JAny<T : Any> : JObjectBiDi<T> {

    private val nodeWriters: AtomicReference<Set<NodeWriter<T>>> = AtomicReference(emptySet())

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): Outcome<JsonError, JsonNodeObject> =
        parseJsonNodeObject(tokensStream, path)

    override fun getWriters(value: T): Set<NodeWriter<T>> = nodeWriters.get()

    private fun registerSetter(nodeWriter: NodeWriter<T>) {
        nodeWriters.getAndUpdate { set -> set + nodeWriter }
    }

    internal fun <FT> registerProperty(jsonProperty: JsonProperty<FT>, binder: (T) -> FT) {
        registerSetter { jno, obj -> jsonProperty.setter(binder(obj))(jno) }
    }

}


sealed class JsonProperty<T> {
    abstract val propName: String
    abstract fun setter(value: T): (JsonNodeObject) -> JsonNodeObject
    abstract fun getter(wrapped: JsonNodeObject): JsonOutcome<T>
    abstract fun parser(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNode>
}

data class JsonParsingException(val error: JsonError) : RuntimeException()


data class JsonPropMandatory<T : Any, JN : JsonNode>(override val propName: String, val jf: BiDiJson<T, JN>) :
    JsonProperty<T>() {

    @Suppress("UNCHECKED_CAST")
    override fun getter(wrapped: JsonNodeObject): Outcome<JsonError, T> =
        wrapped.fieldMap[propName]
            ?.let { idn ->
                jf.fromJsonNode(idn as JN)
            }
            ?: JsonError(wrapped, "Not found $propName").asFailure()

    override fun setter(value: T): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            wrapped.copy(fieldMap = wrapped.fieldMap + (propName to jf.toJsonNode(value, Node(propName, wrapped.path))))
        }

    override fun parser(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNode> =
        jf.parseToNode(tokensStream, path)

}


data class JsonPropOptional<T : Any, JN : JsonNode>(override val propName: String, val jf: BiDiJson<T, JN>) :
    JsonProperty<T?>() {

    @Suppress("UNCHECKED_CAST")
    override fun getter(wrapped: JsonNodeObject): Outcome<JsonError, T?> =
        wrapped.fieldMap[propName]
            ?.let { idn -> jf.fromJsonNode(idn as JN) }
            ?: null.asSuccess()

    override fun setter(value: T?): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            value?.let {
                wrapped.copy(
                    fieldMap = wrapped.fieldMap + (propName to jf.toJsonNode(
                        it,
                        Node(propName, wrapped.path)
                    ))
                )
            } ?: wrapped
        }

    override fun parser(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNode> =
        tokensStream.run {
            if (peek() == "null") parseJsonNodeNull(tokensStream, path)
            else
                jf.parseToNode(tokensStream, path)
        }


}

sealed class JFieldBase<T, PT : Any>
    : ReadOnlyProperty<JAny<PT>, JsonProperty<T>> {

    protected abstract val binder: (PT) -> T

    protected abstract fun buildJsonProperty(property: KProperty<*>): JsonProperty<T>

    operator fun provideDelegate(thisRef: JAny<PT>, prop: KProperty<*>): JFieldBase<T, PT> {
        val jp = buildJsonProperty(prop)
        thisRef.registerProperty(jp, binder)
        return this
    }

    override fun getValue(thisRef: JAny<PT>, property: KProperty<*>): JsonProperty<T> =
        buildJsonProperty(property)
}

class JField<T : Any, PT : Any>(
    override val binder: (PT) -> T,
    private val biDiJson: BiDiJson<T, *>,
    private val jsonFieldName: String? = null
) : JFieldBase<T, PT>() {

    override fun buildJsonProperty(property: KProperty<*>): JsonProperty<T> =
        JsonPropMandatory(jsonFieldName ?: property.name, biDiJson)

}

class JFieldMaybe<T : Any, PT : Any>(
    override val binder: (PT) -> T?,
    private val biDiJson: BiDiJson<T, *>,
    private val jsonFieldName: String? = null
) : JFieldBase<T?, PT>() {

    override fun buildJsonProperty(property: KProperty<*>): JsonProperty<T?> =
        JsonPropOptional(jsonFieldName ?: property.name, biDiJson)

}

