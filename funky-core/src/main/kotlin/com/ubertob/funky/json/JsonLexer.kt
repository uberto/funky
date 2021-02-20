class JsonLexer {

    fun tokenize(jsonStr: String): Sequence<String> =
        sequence {
            var currWord = ""
            jsonStr.forEach {
                when (it) {
                    ' ', '\t', '\n', '\r' -> currWord = yieldIfNotEmpty(currWord)
                    '{', '}', '[', ']', ',', ':' -> {
                        currWord = yieldIfNotEmpty(currWord)
                        yield(it.toString())
                    }
                    '"' -> {
                        currWord = yieldIfNotEmpty(currWord)
                        yield(it.toString())
                    }
                    else -> currWord += it
                }
            }
            yieldIfNotEmpty(currWord)
        }

    private suspend fun SequenceScope<String>.yieldIfNotEmpty(currWord: String): String {
        if (currWord.isNotEmpty()) {
            yield(currWord)
        }
        return ""
    }

}