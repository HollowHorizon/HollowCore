package ru.hollowhorizon.hc.client.imgui

import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ImGuiAnimator(
    private val range: IntRange,
    private val time: Float,
    private val type: Type,
    private val interpolation: Interpolation,
) : ReadWriteProperty<Any?, Float> {
    private var startTime = GLFW.glfwGetTime()

    enum class Type {
        LOOP, FREEZE, REVERSE
    }

    fun reset() {
        startTime = GLFW.glfwGetTime()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    val value: Float
        get() {
            val elapsedTime = (GLFW.glfwGetTime() - startTime).toFloat()
            val percent = when (type) {
                Type.LOOP -> (elapsedTime % time) / time
                Type.FREEZE -> elapsedTime.coerceAtMost(time) / time
                Type.REVERSE -> {
                    val cycleTime = (elapsedTime % (2 * time))
                    if (cycleTime > time) (2 * time - cycleTime) / time else cycleTime / time
                }
            }
            return range.first + (range.last - range.first) * interpolation(percent)
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        reset()
    }
}