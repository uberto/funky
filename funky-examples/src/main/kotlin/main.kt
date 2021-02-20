fun main() {

    val n = 20
    val ss = fizzbuzzSeq().take(n).toList()


    val reversed = ss.reversed().map(String::reversed)

    println(reversed.joinToString(separator = " "))

    java.util.ListIterator
}

fun fizzbuzzSeq(): Sequence<String> =
    generateSequence(1) { it + 1 }
        .map { it.toString() }


fun Int.fizzbuzz(): String =
    when {
        this % 15 == 0 -> "fizzbuzz"
        this % 3 == 0 -> "fizz"
        this % 5 == 0 -> "buzz"
        else -> toString()
    }