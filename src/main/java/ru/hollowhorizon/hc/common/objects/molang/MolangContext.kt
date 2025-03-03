package ru.hollowhorizon.hc.common.objects.molang

import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.particles.Transform
import java.util.*
import kotlin.random.Random
import kotlin.reflect.KProperty

class MolangContext(
    val query: MolangQuery,
    val variables: Variables = VariablesMap(),
)

interface MolangQuery {
    object Empty : MolangQuery
}

interface MolangQueryRandom : MolangQuery {
    val random: Random
}

interface MolangQueryAnimation : MolangQuery {
    val animTime: Float
    val animLoopTime: Float
}

interface MolangQueryTime : MolangQuery {
    val time: Float

    companion object {
        val GLFW_TIME = object : MolangQueryTime {
            override val time: Float get() = GLFW.glfwGetTime().toFloat()
        }
    }
}

interface MolangQueryEntity : MolangQuery, MolangQueryTime {
    val lifeTime: Float
    val modifiedDistanceMoved: Float
    val modifiedMoveSpeed: Float
    val transform: Transform
    val uuid: UUID?

    override val time: Float
        get() = lifeTime

    companion object {
        val EMPTY = object : MolangQueryEntity {
            override val lifeTime = 0f
            override val modifiedDistanceMoved = 0f
            override val modifiedMoveSpeed: Float = 0f
            override val transform = Transform.Zero
            override val uuid: UUID? = null
        }
    }
}

interface Variables {
    fun getOrNull(name: String): Variable?
    fun getOrPut(name: String, initialValue: Float = 0f): Variable
    operator fun get(name: String): Float = getOrNull(name)?.get() ?: Float.NaN
    operator fun set(name: String, value: Float) = getOrPut(name).set(value)
    fun fallbackBackTo(fallback: Variables): Variables = VariablesWithFallback(this, fallback)

    interface Variable {
        fun get(): Float
        fun set(value: Float)

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = get()
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) = set(value)
    }
}

class VariablesMap : Variables {
    private val map = mutableMapOf<String, Variable>()

    override fun getOrNull(name: String): Variables.Variable? = map[name]

    override fun getOrPut(name: String, initialValue: Float): Variables.Variable =
        map.getOrPut(name) { Variable(initialValue) }

    private class Variable(var field: Float) : Variables.Variable {
        override fun get(): Float = field
        override fun set(value: Float) {
            field = value
        }
    }
}

private class VariablesWithFallback(val primary: Variables, val fallback: Variables) : Variables {
    override fun getOrNull(name: String): Variables.Variable? = primary.getOrNull(name) ?: fallback.getOrNull(name)

    override fun getOrPut(name: String, initialValue: Float): Variables.Variable =
        getOrNull(name) ?: primary.getOrPut(name, initialValue)
}