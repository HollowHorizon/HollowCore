package ru.hollowhorizon.hc.client.utils

import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class GuiAnimator protected constructor(
    val begin: Int,
    val end: Int,
    time: Float,
    protected val interpolation: (Float) -> Float,
) : ReadOnlyProperty<Any?, Int> {
    var value: Int = begin
    val maxTime = time * 20
    protected var timePassed: Float = maxTime
    protected var startTicks = ClientTickHandler.ticks

    open fun update(partialTick: Float) {
        if (timePassed > 0) value = begin + ((end - begin) * interpolation(1 - timePassed / maxTime)).toInt()
        timePassed = ClientTickHandler.ticks - startTicks + partialTick
    }

    fun setTime(v: Float) {
        this.timePassed = v * 20
    }

    fun isFinished(): Boolean {
        return timePassed <= 0
    }

    fun reset() {
        startTicks = ClientTickHandler.ticks
        timePassed = maxTime
        value = begin
    }

    class Reversed(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        private var switch = false

        override fun update(partialTick: Float) {
            if (switch) {
                if (timePassed > 0) {
                    value = begin + ((end - begin) * interpolation(timePassed / maxTime)).toInt()
                }
                timePassed = ClientTickHandler.ticks - startTicks + partialTick
            } else super.update(partialTick)

            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }

    class Looped(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        override fun update(partialTick: Float) {
            super.update(partialTick)
            if (isFinished()) {
                reset()
            }
        }
    }

    class Single(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        update(mc.partialTick)
        return value
    }
}