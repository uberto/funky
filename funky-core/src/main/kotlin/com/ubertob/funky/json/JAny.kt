package com.ubertob.funky.json

import com.ubertob.funky.outcome.Outcome
import com.ubertob.funky.outcome.OutcomeError
import com.ubertob.funky.outcome.asFailure
import com.ubertob.funky.outcome.asSuccess
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


typealias NodeWriter<T> = (JsonNodeObject, T) -> JsonNodeObject
typealias NodeReader<T> = (JsonNodeObject) -> T

abstract class JAny<T : Any> : BiDiJson<T, JsonNodeObject> {

    private val nodeWriters: AtomicReference<Set<NodeWriter<T>>> = AtomicReference(emptySet())
    private val nodeReaders: AtomicReference<Set<NodeReader<*>>> = AtomicReference(emptySet())
    private val fieldParsers: AtomicReference<Map<String, TokenStreamParser<JsonNode>>> = AtomicReference(emptyMap())

    internal fun registerSetter(nodeWriter: NodeWriter<T>) {
        nodeWriters.getAndUpdate { set -> set + nodeWriter }
    }

    internal fun registerGetter(nodeReader: NodeReader<*>) {
        nodeReaders.getAndUpdate { set -> set + nodeReader }
    }

    internal fun registerParser(fieldName: String, parser: TokenStreamParser<JsonNode>) {
        fieldParsers.getAndUpdate { map -> map + (fieldName to parser) }
    }

    override fun fromJsonNode(node: JsonNodeObject): JsonOutcome<T> =
        tryFromNode(node) {
            node.deserialize() ?: throw JsonParsingException(
                JsonError(node, "tryDeserialize returned null!")
            )
        }


    override fun toJsonNode(value: T, path: NodePath): JsonNodeObject =
        nodeWriters.get()
            .fold(JsonNodeObject(emptyMap(), path)) { acc, writer ->
                writer(acc, value)
            }

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): Outcome<JsonError, JsonNodeObject> =
        parseJsonNodeObject(tokensStream, fieldParsers.get(), path)


    //it's safe to throw exceptions, they will be caught
    abstract fun JsonNodeObject.deserialize(): T?


    private fun multipleErrors(jsonNode: JsonNodeObject, errors: List<OutcomeError>): JsonError =
        JsonError(jsonNode, errors.joinToString(prefix = "Multiple errors: "))

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
        thisRef.registerSetter { jno, obj -> jp.setter(binder(obj))(jno) }
        thisRef.registerGetter(jp::getter)
        thisRef.registerParser(jp.propName, jp::parser)
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

