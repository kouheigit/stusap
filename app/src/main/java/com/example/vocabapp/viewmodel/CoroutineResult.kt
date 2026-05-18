package com.example.vocabapp.viewmodel

import kotlin.coroutines.cancellation.CancellationException

internal inline fun <T> Result<T>.onFailureUnlessCancellation(
    action: (Throwable) -> Unit
): Result<T> = onFailure { throwable ->
    if (throwable is CancellationException) throw throwable
    action(throwable)
}
