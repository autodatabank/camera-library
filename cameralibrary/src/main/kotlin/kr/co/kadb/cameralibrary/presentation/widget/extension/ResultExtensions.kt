package kr.co.kadb.cameralibrary.presentation.widget.extension

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow

// Based on: https://proandroiddev.com/resilient-use-cases-with-kotlin-result-coroutines-and-annotations-511df10e2e16

/**
 * Like [runCatching], but with proper coroutines cancellation handling.
 * Also only catches [Exception] instead of [Throwable].
 *
 * Cancellation exceptions need to be rethrown. See https://github.com/Kotlin/kotlinx.coroutines/issues/1814.
 */
internal inline fun <R> resultOf(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (t: TimeoutCancellationException) {
        Result.failure(t)
    } catch (c: CancellationException) {
        throw c
    } catch (e: Exception) {
        Result.failure(e)
    }
}

internal suspend inline fun <T> Flow<Result<T>>.collectResult(
    crossinline block: suspend Result<T>.() -> Unit
) {
    collect {
        block(it)
    }
}
