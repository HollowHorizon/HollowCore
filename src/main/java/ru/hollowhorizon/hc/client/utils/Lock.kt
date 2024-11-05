package ru.hollowhorizon.hc.client.utils

import java.util.concurrent.ConcurrentLinkedQueue

class Lock(private var isLocked: Boolean = false) {
    private val actions = ConcurrentLinkedQueue<() -> Unit>()

    fun withLock(action: () -> Unit) {
        if (isLocked) {
            actions += action
            return
        }
        isLocked = true
        try {
            action()
            actions.forEach { it() }
            actions.clear()
        } finally {
            isLocked = false
        }
    }
}