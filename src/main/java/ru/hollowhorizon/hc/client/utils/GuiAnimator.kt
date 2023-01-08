package ru.hollowhorizon.hc.client.utils

open class GuiAnimator(
    val begin: Int,
    val end: Int,
    protected val time: Float,
    protected val interpolation: (Float) -> Float
) {
    var value: Int = begin
    protected var timePassed: Int = (time * 20).toInt()

    open fun update() {
        if (timePassed > 0) {
            value = begin + ((end - begin) * interpolation(1 - timePassed.toFloat() / (time * 20))).toInt()
            timePassed--
        }
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

        override fun update() {
            if (switch) {
                if (timePassed > 0) {
                    value = end - ((end - begin) * interpolation(1 - timePassed.toFloat() / (time * 20))).toInt()
                    timePassed--
                }
            } else {
                super.update()
            }
            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }
}