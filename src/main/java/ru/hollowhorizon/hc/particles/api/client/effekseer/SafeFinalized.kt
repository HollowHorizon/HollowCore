package ru.hollowhorizon.hc.particles.api.client.effekseer

import net.minecraft.client.Minecraft
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

abstract class SafeFinalized<T : Any> protected constructor(kept: T, private val closer: (T) -> Unit) : Closeable {
    private val kept = AtomicReference(kept)

    init {
        KEEPER.add(kept)
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        val kept = kept.get()
        if (kept != null) {
            Minecraft.getInstance().tell(::close)
        }
    }

    override fun close() {
        val removed = kept.getAndSet(null) ?: return
        try {
            closer(removed)
        } finally {
            KEEPER.remove(removed)
        }
    }

    companion object {
        private val KEEPER: MutableSet<Any> = Collections.newSetFromMap(ConcurrentHashMap())
    }
}
