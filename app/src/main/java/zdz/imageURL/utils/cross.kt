package zdz.imageURL.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

inline fun <S, T> Sequence<S>.crossFirstOrNull(
    other: Iterable<T>, predicate: (S, T) -> Boolean
): Pair<S, T>? = firstNotNullOfOrNull { t -> other.find { predicate(t, it) }?.let { t to it } }

inline fun <S, T> Sequence<S>.crossFind(
    other: Iterable<T>,
    predicate: (S, T) -> Boolean
): Pair<S, T>? = crossFirstOrNull(other, predicate)

inline fun <S, T> Sequence<S>.crossFirst(
    other: Iterable<T>,
    predicate: (S, T) -> Boolean
): Pair<S, T> = crossFind(other, predicate)
    ?: throw NoSuchElementException("No element matching predicate was found.")

inline fun <S, T, R> Sequence<S>.crossFirstNotNullOfOrNull(
    other: Iterable<T>, transform: (S, T) -> R?
): R? = firstNotNullOfOrNull { t -> other.firstNotNullOfOrNull { transform(t, it) } }

inline fun <S, T, R> Sequence<S>.crossFirstNotNullOf(
    other: Iterable<T>,
    transform: (S, T) -> R?
): R = crossFirstNotNullOfOrNull(other, transform)
    ?: throw NoSuchElementException("No element matching predicate was found.")

inline fun <S, T> Sequence<S>.crossFilter(
    other: Iterable<T>, crossinline predicate: (S, T) -> Boolean
) = sequence { for (s in this@crossFilter) for (t in other) if (predicate(s, t)) yield(s to t) }

inline fun <S, T> Sequence<S>.crossSingle(
    other: Iterable<T>,
    predicate: (S, T) -> Boolean
): Pair<S, T> {
    var result: Pair<S, T>? = null
    for (s in this) {
        for (t in other) {
            if (predicate(s, t)) {
                if (result != null) throw IllegalStateException("more than one element")
                result = s to t
            }
        }
    }
    return result ?: throw NoSuchElementException("No element matching predicate was found.")
}

inline fun <S, T> Sequence<S>.crossSingleOrNull(
    other: Iterable<T>, predicate: (S, T) -> Boolean
): Pair<S, T>? {
    var result: Pair<S, T>? = null
    for (s in this) {
        for (t in other) {
            if (predicate(s, t)) {
                if (result != null) return null
                result = s to t
            }
        }
    }
    return result
}

@PublishedApi
internal data object AbortFlowException : CancellationException() {
    private fun readResolve(): Any = AbortFlowException
}

suspend inline fun <T> Flow<T>.collectWhile(crossinline predicate: suspend (value: T) -> Boolean) =
    try {
        collect { value -> if (!predicate(value)) throw AbortFlowException }
    } catch (_: AbortFlowException) {
        // ignore
    }

suspend inline fun <S, T> Flow<S>.crossFirstOrNull(
    other: Iterable<T>, crossinline predicate: suspend (S, T) -> Boolean
): Pair<S, T>?  = crossFirstNotNullOfOrNull(other) { s, t -> if (predicate(s, t)) s to t else null }

suspend inline fun <S, T> Flow<S>.crossFind(
    other: Iterable<T>,
    crossinline predicate: suspend (S, T) -> Boolean
): Pair<S, T>? = crossFirstOrNull(other, predicate)

suspend inline fun <S, T> Flow<S>.crossFirst(
    other: Iterable<T>,
    crossinline predicate: suspend (S, T) -> Boolean
): Pair<S, T> = crossFind(other, predicate)
    ?: throw NoSuchElementException("No element matching predicate was found.")

suspend inline fun <S, T, R> Flow<S>.crossFirstNotNullOfOrNull(
    other: Iterable<T>, crossinline transform: suspend (S, T) -> R?
): R? {
    var result: R? = null
    collectWhile { s ->
        other.firstNotNullOfOrNull { t ->
            transform(s, t)
        }.also { result = it } == null
    }
    return result
}

suspend inline fun <S, T, R> Flow<S>.crossFirstNotNullOf(
    other: Iterable<T>,
    crossinline transform: suspend (S, T) -> R?
): R = crossFirstNotNullOfOrNull(other, transform)
    ?: throw NoSuchElementException("No element matching predicate was found.")

suspend inline fun <S, T> Flow<S>.crossFilter(
    other: Iterable<T>, crossinline predicate: suspend (S, T) -> Boolean
) = flow {
    collect { s ->
        for (t in other) {
            if (predicate(s, t)) emit(s to t)
        }
    }
}

suspend inline fun <S, T> Flow<S>.crossSingle(
    other: Iterable<T>,
    crossinline predicate: suspend (S, T) -> Boolean
): Pair<S, T> {
    var result: Pair<S, T>? = null
    collectWhile {
        for (t in other) {
            if (predicate(it, t)) {
                if (result != null) throw IllegalStateException("more than one element")
                result = it to t
            }
        }
        result == null
    }
    return result ?: throw NoSuchElementException("No element matching predicate was found.")
}

suspend inline fun <S, T> Flow<S>.crossSingleOrNull(
    other: Iterable<T>, crossinline predicate: suspend (S, T) -> Boolean
): Pair<S, T>? {
    var result: Pair<S, T>? = null
    var duplicate = false
    collectWhile { s ->
        for (t in other) {
            if (predicate(s, t)) {
                if (result != null) duplicate = true
                result = s to t
            }
        }
        !duplicate && result == null
    }
    return result.takeIf { !duplicate }
}