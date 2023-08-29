package ru.hollowhorizon.hc.client.utils

open class GuiAnimator protected constructor(
    val begin: Int,
    val end: Int,
    protected val time: Float,
    protected val interpolation: (Float) -> Float,
) {
    var value: Int = begin
    val maxTime = time * 20
    protected var timePassed: Float = maxTime
    protected var current: Int = begin
    protected var last: Int = begin

    open fun update(particalTick: Float) {
        if (timePassed > 0) {
            value = begin + ((end - begin) * interpolation(1 - timePassed / maxTime)).toInt()
        }
        timePassed -= particalTick
    }

    fun setTime(v: Float) {
        this.timePassed = v * 20
    }

    fun isFinished(): Boolean {
        return timePassed <= 0
    }

    fun reset() {
        timePassed = maxTime
        value = begin
    }

    class Reversed(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        private var switch = false

        override fun update(particalTick: Float) {
            if (switch) {
                if (timePassed > 0) {
                    value = begin + ((end - begin) * interpolation(timePassed / maxTime)).toInt()
                }
                timePassed -= particalTick
            } else super.update(particalTick)

            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }

    class Looped(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        override fun update(particalTick: Float) {
            super.update(particalTick)
            if (isFinished()) {
                reset()
            }
        }
    }

    class Single(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation)
}