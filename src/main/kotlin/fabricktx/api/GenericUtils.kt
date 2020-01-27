@file:Suppress("NOTHING_TO_INLINE")

package fabricktx.api

import kotlin.math.max

fun Double.isWholeNumber() = this.toInt().toDouble() == this

fun max3(num1: Int, num2: Int, num3: Int): Int = max(max(num1, num2), num3)

fun String.splitOn(splitter: Char): Pair<String, String> {
    val split = split(splitter)
    require(split.size == 2) { "The string \"$this\" must be split exactly in two by the delimiter '$splitter'" }
    return Pair(split[0],split[1])
}


/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R> Iterable<T>.flatMapIndexed(transform: (Int, T) -> Iterable<R>): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this)
        destination.addAll(transform(index++, item))

    return destination
}


inline fun Int.squared() = this * this
inline fun Double.squared() = this * this


val Int.d get() = this.toDouble()
val Long.d get() = this.toDouble()
val Int.f get() = this.toFloat()
val Float.d get() = this.toDouble()
val Int.l get() = this.toLong()


fun <T> buildList(init: MutableList<T>.() -> Unit) = mutableListOf<T>().apply(init)

inline fun <reified T> Any?.ifIs(callback: (T) -> Unit) = if (this is T) callback(this) else Unit