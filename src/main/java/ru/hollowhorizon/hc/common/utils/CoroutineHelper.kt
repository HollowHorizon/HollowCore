package ru.hollowhorizon.hc.common.utils

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

object CoroutineHelper {
    /**
     * Этот метод является публичным мостом для безопасного возобновления
     * корутин из Java-кода.
     */
    @JvmStatic
    fun resumeUnit(continuation: Continuation<Unit>) {
        continuation.resume(Unit)
    }
}