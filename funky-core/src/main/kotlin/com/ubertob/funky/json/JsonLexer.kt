import LexerState.*

enum class LexerState {
    OutString, InString, Escaping //todo Unicode?
}

class JsonLexer {

    fun tokenize(jsonStr: String): Sequence<String> =
        sequence {
            val currWord = StringBuilder()
            var state = OutString
            jsonStr.forEach { char ->
                when (state) {
                    OutString ->
                        when (char) {
                            ' ', '\t', '\n', '\r', '\b' -> yieldIfNotEmpty(currWord)
                            '{', '}', '[', ']', ',', ':' -> {
                                yieldIfNotEmpty(currWord)
                                yield(char.toString())
                            }
                            '"' -> {
                                yieldIfNotEmpty(currWord)
                                yield(char.toString())
                                state = InString
                            }
                            else -> currWord.append(char)
                        }

                    InString -> when (char) {
                        '\\' -> {
                            state = Escaping
                        }
                        '"' -> {
                            yieldIfNotEmpty(currWord)
                            yield(char.toString())
                            state = OutString
                        }
                        else -> currWord += char
                    }
                    Escaping -> when (char) {
                        '\\' -> currWord += '\\'
                        'n' -> currWord += '\n'
                        'f' -> currWord += '\t'
                        't' -> currWord += '\t'
                        'r' -> currWord += '\r'
                        'b' -> currWord += '\b'
                        '"' -> currWord += '\"'
                        else -> error("Wrong escape char $char")
                    }.also { state = InString }
                }
            }
            yieldIfNotEmpty(currWord)
        }

    private suspend fun SequenceScope<String>.yieldIfNotEmpty(currWord: StringBuilder) {
        if (currWord.isNotEmpty()) {
            yield(currWord.toString())
        }
        currWord.clear()
    }

}

operator fun StringBuilder.plusAssign(c: Char) {
    append(c)
}