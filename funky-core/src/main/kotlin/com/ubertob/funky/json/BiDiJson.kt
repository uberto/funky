package com.ubertob.funky.json


import com.ubertob.funky.outcome.*
import com.ubertob.funky.outcome.Outcome.Companion.tryThis
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface StringWrapper {
    val raw: String
}

data class JsonError(val node: JsonNode?, val reason: String) : OutcomeError {
    val location = node?.path?.joinToString(separator = "/", prefix = "</", postfix = ">") ?: "parsing"
    override val msg = "error at $location reason: $reason"
}

typealias JsonOutcome<T> = Outcome<JsonError, T>

interface BiDiJson<T, JN : JsonNode> {
    fun fromJsonNode(node: JN): JsonOutcome<T>
    fun toJsonNode(value: T): JN
}

typealias NodeWriter<T> = (JsonNodeObject, T) -> JsonNodeObject
typealias NodeReader<T> = (JsonNodeObject) -> JsonOutcome<T>

abstract class JAny<T : Any> : BiDiJson<T, JsonNodeObject> {

    private val nodeWriters: AtomicReference<Set<NodeWriter<T>>> = AtomicReference(emptySet())
    private val nodeReaders: AtomicReference<Set<NodeReader<*>>> = AtomicReference(emptySet())

    internal fun registerSetter(nodeWriter: NodeWriter<T>) {
        nodeWriters.getAndUpdate { set -> set + nodeWriter }
    }

    internal fun registerGetter(nodeReader: NodeReader<*>) {
        nodeReaders.getAndUpdate { set -> set + nodeReader }
    }

    override fun fromJsonNode(node: JsonNodeObject): Outcome<JsonError, T> = node.asObject(::deserialize)

    override fun toJsonNode(value: T): JsonNodeObject = serialize(value)

    fun serialize(value: T): JsonNodeObject = nodeWriters.get()
        .map { nw -> { jno: JsonNodeObject -> nw(jno, value) } }.fold(JsonNodeObject(emptyMap())) { acc, setter ->
            setter(acc)
        }

    abstract fun JsonNodeObject.tryDeserialize(): T?

    fun deserialize(from: JsonNodeObject): Outcome<JsonError, T> =
        composeFailures(nodeReaders.get(), from)
            .bind {
                tryThis {
                    from.tryDeserialize() ?: throw JsonParsingException(
                        JsonError(from, "tryDeserialize returned null!")
                    )
                }.transformFailure { throwableError ->
                    when (throwableError.t) {
                        is JsonParsingException -> throwableError.t.error // keep path info
                        else -> JsonError(from, throwableError.msg)
                    }
                }
            }

    private fun composeFailures(nodeReaders: Set<NodeReader<*>>, jsonNode: JsonNodeObject): JsonOutcome<Unit> =
        nodeReaders
            .fold(emptyList<JsonOutcome<*>>()) { acc, r -> acc + r(jsonNode) }
            .mapNotNull {
                when (it) {
                    is Success -> null
                    is Failure -> it.error
                }
            }
            .let { errors ->
                when {
                    errors.isEmpty() -> Unit.asSuccess()
                    errors.size == 1 -> errors[0].asFailure()
                    else -> multipleErrors(jsonNode, errors).asFailure()
                }
            }

    private fun multipleErrors(jsonNode: JsonNodeObject, errors: List<OutcomeError>): JsonError =
        JsonError(jsonNode, errors.joinToString(prefix = "Multiple errors: "))


}

object JBoolean : BiDiJson<Boolean, JsonNodeBoolean> {

    override fun fromJsonNode(node: JsonNodeBoolean): Outcome<JsonError, Boolean> = node.asBoolean()
    override fun toJsonNode(value: Boolean): JsonNodeBoolean = JsonNodeBoolean(value)

}

object JString : BiDiJson<String, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, String> = node.asText()
    override fun toJsonNode(value: String): JsonNodeString = JsonNodeString(value)

}

object JInt : BiDiJson<Int, JsonNodeInt> {

    override fun fromJsonNode(node: JsonNodeInt): Outcome<JsonError, Int> = node.asInt()
    override fun toJsonNode(value: Int): JsonNodeInt = JsonNodeInt(value)
}


object JLong : BiDiJson<Long, JsonNodeLong> {

    override fun fromJsonNode(node: JsonNodeLong): Outcome<JsonError, Long> = node.asLong()
    override fun toJsonNode(value: Long): JsonNodeLong = JsonNodeLong(value)
}

object JDouble : BiDiJson<Double, JsonNodeDouble> {

    override fun fromJsonNode(node: JsonNodeDouble): Outcome<JsonError, Double> = node.asDouble()
    override fun toJsonNode(value: Double): JsonNodeDouble = JsonNodeDouble(value)
}

data class JStringWrapper<T : StringWrapper>(val cons: (String) -> T) : BiDiJson<T, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, T> = node.asText().transform(cons)
    override fun toJsonNode(value: T): JsonNodeString = JsonNodeString(value.raw)

}

data class JArray<T : Any, JN : JsonNode>(val helper: BiDiJson<T, JN>) : BiDiJson<List<T>, JsonNodeArray<JN>> {

    override fun fromJsonNode(node: JsonNodeArray<JN>): Outcome<JsonError, List<T>> =
        mapFrom(node, helper::fromJsonNode)

    override fun toJsonNode(value: List<T>): JsonNodeArray<JN> = mapToJson(value, helper::toJsonNode)

    private fun <T : Any> mapToJson(objs: List<T>, f: (T) -> JN): JsonNodeArray<JN> =
        JsonNodeArray(objs.map(f))

    private fun <T : Any> mapFrom(
        node: JsonNodeArray<JN>,
        f: (JN) -> Outcome<JsonError, T>
    ): Outcome<JsonError, List<T>> =
        node.asArray<JN>().bind { nodes -> nodes.map { n: JN -> f(n) }.sequence() }
}


sealed class JsonProperty<T> {
    abstract val propName: String
    abstract fun setter(value: T): (JsonNodeObject) -> JsonNodeObject
    abstract fun getter(wrapped: JsonNodeObject): JsonOutcome<T>
}

data class JsonParsingException(val error: JsonError) : RuntimeException()


data class JsonPropMandatory<T : Any, JN : JsonNode>(override val propName: String, val jf: BiDiJson<T, JN>) :
    JsonProperty<T>() {

    @Suppress("UNCHECKED_CAST")
    override fun getter(wrapped: JsonNodeObject): Outcome<JsonError, T> =
        wrapped.fieldMap[propName]
            ?.let { idn ->
                tryThis { jf.fromJsonNode(idn as JN) }
                    .bind { it } //todo add join
                    .transformFailure { JsonError(idn, it.msg) }
            }
            ?: JsonError(wrapped, "Not found $propName").asFailure()

    override fun setter(value: T): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            wrapped.copy(fieldMap = wrapped.fieldMap + (propName to jf.toJsonNode(value)))
        }
}


data class JsonPropOptional<T : Any, JN : JsonNode>(override val propName: String, val jf: BiDiJson<T, JN>) :
    JsonProperty<T?>() {

    @Suppress("UNCHECKED_CAST")
    override fun getter(wrapped: JsonNodeObject): Outcome<JsonError, T?> =
        wrapped.fieldMap[propName]
            ?.let { idn ->
                tryThis { jf.fromJsonNode(idn as JN) }
                    .bind { it }//todo add join
                    .transformFailure { JsonError(idn, it.msg) }
            }
            ?: null.asSuccess()

    override fun setter(value: T?): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            value?.let {
                wrapped.copy(fieldMap = wrapped.fieldMap + (propName to jf.toJsonNode(it)))
            } ?: wrapped
        }

}

sealed class JFieldBase<T, PT : Any>
    : ReadOnlyProperty<JAny<PT>, JsonProperty<T>> {

    protected abstract val binder: (PT) -> T

    protected abstract fun buildJsonProperty(property: KProperty<*>): JsonProperty<T>

    operator fun provideDelegate(thisRef: JAny<PT>, prop: KProperty<*>): JFieldBase<T, PT> {
        val jp = buildJsonProperty(prop)
        thisRef.registerSetter { jno, obj -> jp.setter(binder(obj))(jno) }
        thisRef.registerGetter { jno -> jp.getter(jno) }

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




