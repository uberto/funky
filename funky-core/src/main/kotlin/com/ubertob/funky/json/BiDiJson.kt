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


interface BiDiJson<T> {
    fun fromJson(node: JsonNode): JsonOutcome<T>
    fun toJson(value: T): JsonNode
}

typealias NodeWriter<T> = (JsonNodeObject, T) -> JsonNodeObject
typealias NodeReader<T> = (JsonNodeObject) -> JsonOutcome<T>

abstract class JAny<T : Any> : BiDiJson<T> {

    private val nodeWriters: AtomicReference<Set<NodeWriter<T>>> = AtomicReference(emptySet())
    private val nodeReaders: AtomicReference<Set<NodeReader<*>>> = AtomicReference(emptySet())

    internal fun registerSetter(nodeWriter: NodeWriter<T>) {
        nodeWriters.getAndUpdate { set -> set + nodeWriter }
    }

    internal fun registerGetter(nodeReader: NodeReader<*>) {
        nodeReaders.getAndUpdate { set -> set + nodeReader }
    }

    override fun fromJson(node: JsonNode): Outcome<JsonError, T> = node.asObject(this::deserialize)

    override fun toJson(value: T): JsonNode =
        serialize(value)

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

object JBoolean : BiDiJson<Boolean> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, Boolean> = node.asBoolean()

    override fun toJson(value: Boolean): JsonNode = JsonNodeBoolean(value)

}

object JString : BiDiJson<String> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, String> = node.asText()

    override fun toJson(value: String): JsonNode = JsonNodeString(value)

}

object JInt : BiDiJson<Int> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, Int> = node.asInt()

    override fun toJson(value: Int): JsonNode = JsonNodeInt(value)
}


object JLong : BiDiJson<Long> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, Long> = node.asLong()

    override fun toJson(value: Long): JsonNode = JsonNodeLong(value)
}

object JDouble : BiDiJson<Double> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, Double> = node.asDouble()

    override fun toJson(value: Double): JsonNode = JsonNodeDouble(value)
}

data class JStringWrapper<T : StringWrapper>(val cons: (String) -> T) : BiDiJson<T> {

    override fun fromJson(node: JsonNode): Outcome<JsonError, T> =
        node.asText().transform(cons)

    override fun toJson(value: T): JsonNode = JsonNodeString(value.raw)

}

data class JArray<T : Any>(val helper: BiDiJson<T>) : BiDiJson<List<T>> {
    override fun fromJson(node: JsonNode): Outcome<JsonError, List<T>> = mapFrom(node, helper::fromJson)

    override fun toJson(value: List<T>): JsonNode = mapToJson(value, helper::toJson)

    private fun <T : Any> mapToJson(objs: List<T>, f: (T) -> JsonNode): JsonNode =
        JsonNodeArray(objs.map(f))

    private fun <T : Any> mapFrom(
        node: JsonNode,
        f: (JsonNode) -> Outcome<JsonError, T>
    ): Outcome<JsonError, List<T>> =
        node.asArray().bind { nodes -> nodes.map { n: JsonNode -> f(n) }.sequence() }
}


sealed class JsonProperty<T> {
    abstract val propName: String
    abstract fun setter(value: T): (JsonNodeObject) -> JsonNodeObject
    abstract fun getter(wrapped: JsonNodeObject): JsonOutcome<T>
}

data class JsonParsingException(val error: JsonError) : RuntimeException()


data class JsonPropMandatory<T : Any>(override val propName: String, val jf: BiDiJson<T>) : JsonProperty<T>() {

    override fun getter(node: JsonNodeObject): Outcome<JsonError, T> =
        node.fieldMap[propName]
            ?.let { idn -> jf.fromJson(idn) }
            ?: JsonError(node, "Not found $propName").asFailure()

    override fun setter(value: T): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            wrapped.copy(fieldMap = wrapped.fieldMap + (propName to jf.toJson(value)))
        }
}


data class JsonPropOptional<T : Any>(override val propName: String, val jf: BiDiJson<T>) : JsonProperty<T?>() {

    override fun getter(node: JsonNodeObject): Outcome<JsonError, T?> =
        node.fieldMap[propName]
            ?.let { idn -> jf.fromJson(idn) }
            ?: null.asSuccess()

    override fun setter(value: T?): (JsonNodeObject) -> JsonNodeObject =
        { wrapped ->
            value?.let {
                wrapped.copy(fieldMap = wrapped.fieldMap + (propName to jf.toJson(it)))
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
    private val biDiJson: BiDiJson<T>,
    private val jsonFieldName: String? = null
) : JFieldBase<T, PT>() {

    override fun buildJsonProperty(property: KProperty<*>): JsonProperty<T> =
        JsonPropMandatory(jsonFieldName ?: property.name, biDiJson)

}

class JFieldMaybe<T : Any, PT : Any>(
    override val binder: (PT) -> T?,
    private val biDiJson: BiDiJson<T>,
    private val jsonFieldName: String? = null
) : JFieldBase<T?, PT>() {

    override fun buildJsonProperty(property: KProperty<*>): JsonProperty<T?> =
        JsonPropOptional(jsonFieldName ?: property.name, biDiJson)

}




