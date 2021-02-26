package com.ubertob.funky.json

/*
object JBigDecimal : BiDiJson<BigDecimal, JsonNodeString> {

    override fun fromJsonNode(node: JsonNodeString): Outcome<JsonError, BigDecimal> = node.asText().transform(::BigDecimal)
    override fun toJsonNode(value: BigDecimal, path: NodePath): JsonNodeString = JsonNodeString(value.toString(), path)
    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeString> =
        JString.parseToNode(tokensStream, path)

}



object JCurrency : BiDiJson<Currency, JsonNodeString> {
    override fun extract(wrapped: JsonNodeString): Outcome<JsonError, Currency> = wrapped.asText().map(Currency::getInstance)

    override fun build(value: Currency): JsonNodeString = JsonNodeString(value.currencyCode)
}

data class JEnum<E : Enum<E>>(val cons: (String) -> E) : BiDiJson<E, JsonNodeString> {
    override fun extract(wrapped: JsonNodeString): Outcome<JsonError, E> =
        wrapped.asText().map(cons)

    override fun build(value: E): JsonNodeString =
        JsonNodeString(value.name)

}



object JInstant : BiDiJson<Instant, JsonNodeLong> {

    override fun extract(wrapped: AbstractJsonNode): Outcome<JsonError, Instant> =

        wrapped.asText().flatMapTry(Instant::parse,
            onError = { s, t ->
                JsonError(wrapped, "exception parsing $s, ${t.message}")
            }
        )

    override fun build(value: Instant): AbstractJsonNode = JsonNodeString(value.toString())

}


object JInstantD : BiDiJson<Instant, JsonNodeString> {
    override fun fromJsonNode(node: JsonNodeString): JsonOutcome<Instant> {
        TODO("Not yet implemented")
    }

    override fun toJsonNode(value: Instant, path: NodePath): JsonNodeString {
        TODO("Not yet implemented")
    }

    override fun parseToNode(tokensStream: TokensStream, path: NodePath): JsonOutcome<JsonNodeString> {
        TODO("Not yet implemented")
    }

}


object JLocalDate : BiDiJson<LocalDate, JsonNodeString> {

    override fun extract(wrapped: JsonNodeString): Outcome<JsonError, LocalDate> =

        wrapped.asText().flatMapTry(LocalDate::parse,
            onError = { s, t ->
                JsonError(wrapped, "exception parsing $s, ${t.message}")
            }
        )

    override fun build(value: LocalDate): JsonNodeString = JsonNodeString(value.toString())

}


data class JSingleton<T : Any>(val singleton: T) : JAny<T>() {
    override fun deserialize1(from: JsonNodeObject): Outcome<JsonError, T> =
        singleton.asSuccess()

    override fun serialize(aValue: T): JsonNodeObject =
        JsonNodeObject(emptyMap())
}


interface JSealed<T : Any> : JAny<T> {

    val typeName: String
        get() = "type"

    val subtypesMap: Map<String, JProtocol<out T>>

    fun <U : T> serializeSubtype(subtype: String, aValue: U): JsonNodeObject {
        val conv = subtypesMap[subtype] ?: throw UnknownSubtypeException("subtype not known $subtype")
        val jsonNodeObject = convertToNodeObj(conv, subtype, aValue)
        return writeObjNode(typeName to JString.build(subtype), *jsonNodeObject.fieldMap.toList().toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    fun <U : T> convertToNodeObj(conv: JProtocol<out T>, subtype: String, aValue: U): JsonNodeObject =
        (conv as? JProtocol<U>
            ?: throw UnknownSubtypeException("subtype $subtype does not match with $aValue")).serialize(aValue)

    override fun deserialize1(from: JsonNodeObject): Outcome<JsonError, T> =
        JString.getFieldFromNode(from, typeName).flatMap { subtype ->
            subtypesMap.get(subtype)
                ?.deserialize1(from)
                ?: JsonError(from, "subtype not known $subtype").asFailure()
        }
}

class JProperties<T : Any>(private val valueConverter: BiDiJson<T, JsonNodeObject>) : BiDiJson<Map<String, T>> {
    override fun extract(wrapped: JsonNodeObject): Outcome<JsonError, Map<String, T>> =
        wrapped.asObject {
            fieldMap.mapValues { entry ->
                valueConverter.extract(entry.value)
                    .onFailure { return@asObject it }
            }.asSuccess()
        }

    override fun build(value: Map<String, T>): JsonNodeObject =
        JsonNodeObject(value.mapValues { valueConverter.build(it.value) })
}

*/

//
//abstract class JAny<T : Any> : JProtocol<T> {
//
//    private val nodeWriters: AtomicReference<Set<NodeWriter<T>>> = AtomicReference(emptySet())
//    private val nodeReaders: AtomicReference<Set<NodeReader<*>>> = AtomicReference(emptySet())
//
//    internal fun registerSetter(nodeWriter: NodeWriter<T>) {
//        nodeWriters.getAndUpdate { set -> set + nodeWriter }
//    }
//
//    internal fun registerGetter(nodeReader: NodeReader<*>) {
//        nodeReaders.getAndUpdate { set -> set + nodeReader }
//    }
//
//    override fun deserialize1(from: JsonNodeObject): Result<JsonError, T> =
//        from.deserialize()
//
//
//    // TODO activate the new style one by one and then remove the open and TODO() and leave it as abstract
//    open fun JsonNodeObject.tryDeserialize(): T? = TODO("tryDeserialize not implemented")
//
//    override fun JsonNodeObject.deserialize(): Result<JsonError, T> =
//        tryCatchResult({
//            tryDeserialize() ?: throw JsonParsingException(JsonError(this, "tryDeserialize returned null!"))
//        }, { throwable ->
//            when (throwable) {
//                is JsonParsingException -> throwable.error // to keep path info
//                else                    -> JsonError(this, throwable.message.orEmpty())
//            }
//        })
//
//
//    override fun serialize(aValue: T): JsonNodeObject =
//        nodeWriters.get()
//            .map { nw -> { jno: JsonNodeObject -> nw(jno, aValue) } }.fold(JsonNodeObject(emptyMap())) { acc, setter ->
//                setter(acc)
//            }
//}

//
//data class UnknownSubtypeException(override val message: String?) : RuntimeException()
//
//
////---
//
//interface Lens<A, B : Any, C : Result<*, A>> {
//    fun setter(value: A): (B) -> B
//    fun getter(wrapped: B): C
//}
//
//sealed class JsonProperty<T> : Lens<T, JsonNodeObject, JsonResult<T>> {
//    abstract val propName: String
//
//    abstract fun setTo(value: T): Pair<String, AbstractJsonNode>? //todo switch to setter
//    abstract fun getFrom(node: JsonNodeObject): Result<JsonError, T>  //todo switch to setter
//}
//
//
//private data class JsonProp<T : Any>(override val propName: String, val conv: JsonAdjoint<T>) : JsonProperty<T>() {
//
//    override fun getFrom(node: JsonNodeObject): Result<JsonError, T> =
//        conv.getFieldFromNode(node, propName)
//
//    override fun setTo(value: T): Pair<String, AbstractJsonNode>? =
//        propName to conv.build(value)
//
//    override fun getter(wrapped: JsonNodeObject): JsonResult<T> = getFrom(wrapped)
//
//    override fun setter(value: T): (JsonNodeObject) -> JsonNodeObject =
//        { wrapped ->
//            wrapped.copy(fieldMap = wrapped.fieldMap + (propName to conv.build(value)))
//        }
//
//}

//private data class JsonPropOptional<T : Any>(override val propName: String, val conv: JsonAdjoint<T>) : JsonProperty<T?>() {
//
//    override fun getFrom(node: JsonNodeObject): Result<JsonError, T?> =
//        node.fieldMap[propName]
//            ?.let { idn -> conv.extract(idn) }
//            ?: null.asSuccess()
//
//    override fun setTo(value: T?): Pair<String, AbstractJsonNode>? =
//        value?.let {
//            propName to conv.build(it)
//        }
//
//    override fun getter(wrapped: JsonNodeObject): JsonResult<T?> = getFrom(wrapped)
//
//    override fun setter(value: T?): (JsonNodeObject) -> JsonNodeObject = { wrapped ->
//        value?.let {
//            wrapped.copy(fieldMap = wrapped.fieldMap + (propName to conv.build(it)))
//        } ?: wrapped
//    }
//
//}
//
//fun <T : Any> JsonAdjoint<T>.getFieldFromNode(node: JsonNodeObject, propName: String): Result<JsonError, T> {
//    return (node.fieldMap[propName]
//        ?.let { idn -> extract(idn) }
//        ?: JsonError(node, "Not found $propName").asFailure())
//}