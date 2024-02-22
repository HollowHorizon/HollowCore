package ru.hollowhorizon.hc.client.utils

import ru.hollowhorizon.hc.client.handlers.ClientTickHandler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class GuiAnimator protected constructor(
    val begin: Int,
    val end: Int,
    val maxTime: Int,
    protected val interpolation: (Float) -> Float,
) : ReadOnlyProperty<Any?, Int> {
    var value: Float = begin.toFloat()
    private var startTicks = ClientTickHandler.clientTicks

    open fun update(partialTick: Float) {
        if (isFinished()) return

        val currentTime = ClientTickHandler.clientTicks - startTicks + partialTick
        value = begin + (end - begin) * interpolation(currentTime / maxTime)
    }

    fun isFinished(): Boolean {
        return ClientTickHandler.clientTicks - startTicks > maxTime
    }

    fun reset() {
        startTicks = ClientTickHandler.clientTicks
        value = begin.toFloat()
    }

    class Reversed(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        private var switch = false

        override fun update(partialTick: Float) {
            super.update(partialTick)
            if (switch) value = end - value

            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }

    class Looped(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        override fun update(partialTick: Float) {
            super.update(partialTick)
            if (isFinished()) reset()
        }
    }

    class Single(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        update(mc.partialTick)
        return value.toInt()
    }
}