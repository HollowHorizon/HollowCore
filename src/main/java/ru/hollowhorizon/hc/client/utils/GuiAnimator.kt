package ru.hollowhorizon.hc.client.utils

open class GuiAnimator(
    val begin: Int,
    val end: Int,
    protected val time: Float,
    protected val interpolation: (Float) -> Float,
) {
    var value: Int = begin
    protected var timePassed: Int = (time * 20).toInt()
    protected var lastValue: Int = begin

    open fun update(particalTick: Float) {
        if (timePassed > 0) {
            val current = begin + ((end - begin) * interpolation(1 - timePassed.toFloat() / (time * 20))).toInt()
            value = (lastValue + (current - lastValue) * particalTick).toInt()
            lastValue = value
        }
    }

    fun tick() {
        if (timePassed > 0) timePassed--
    }

    fun isFinished(): Boolean {
        return timePassed <= 0
    }

    fun reset() {
        timePassed = (time * 20).toInt()
    }

    class Looped(begin: Int, end: Int, time: Float, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        private var switch = false

        override fun update(particalTick: Float) {
            if (switch) {
                if (timePassed > 0) {
                    val current = end - ((end - begin) * interpolation(1 - timePassed.toFloat() / (time * 20))).toInt()
                    value = (lastValue + (current - lastValue) * particalTick).toInt()
                    lastValue = value

                }

            } else {
                super.update(particalTick)
            }
            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }
}