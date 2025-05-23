package ru.hollowhorizon.hc.common.utils.molang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.codehaus.janino.ClassBodyEvaluator
import java.lang.ref.SoftReference

fun interface FloatExpr {
    fun compute(q: EntityQuery): Float
}

fun interface BooleanExpr {
    fun compute(q: EntityQuery): Boolean
}

object MolangCompilerScope : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob())

open class Molang(private val parent: ClassLoader? = null) {
    private val floatCache = mutableMapOf<String, SoftReference<FloatExpr>>()
    private val boolCache = mutableMapOf<String, SoftReference<BooleanExpr>>()

    fun compileFloat(text: String): (EntityQuery) -> Float {
        floatCache[text]?.get()?.let { cached ->
            return { q -> cached.compute(q) }
        }

        val safeHash = text.hashCode().toUInt()
        val className = "FloatExpr_$safeHash"

        val cbe = ClassBodyEvaluator().apply {
            setImplementedInterfaces(arrayOf(FloatExpr::class.java))
            setClassName(className)
            setParentClassLoader(parent ?: this@Molang.javaClass.classLoader)
            setDefaultImports(
                "java.lang.*",
                "ru.hollowhorizon.hc.common.utils.molang.*",
                "static java.lang.Math.*"
            )
        }

        val source = """
            public float compute(EntityQuery q) {
                return (float)($text);
            }
        """.trimIndent()
        println(source)
        cbe.cook(source)

        val instance = cbe.clazz.getDeclaredConstructor().newInstance() as FloatExpr
        floatCache[text] = SoftReference(instance)

        return { q -> instance.compute(q) }
    }

    fun compileBoolean(text: String): (EntityQuery) -> Boolean {
        boolCache[text]?.get()?.let { cached ->
            return { q -> cached.compute(q) }
        }

        val safeHash = text.hashCode().toUInt()
        val className = "BoolExpr_$safeHash"

        val cbe = ClassBodyEvaluator().apply {
            setImplementedInterfaces(arrayOf(BooleanExpr::class.java))
            setClassName(className)
            setParentClassLoader(parent ?: this@Molang.javaClass.classLoader)
            setDefaultImports(
                "java.lang.*",
                "ru.hollowhorizon.hc.common.utils.molang.*",
                "static java.lang.Math.*"
            )
        }

        val source = """
            public boolean compute(${EntityQuery::class.java.name} q) {
                return ($text);
            }
        """.trimIndent()
        cbe.cook(source)

        val instance = cbe.clazz.getDeclaredConstructor().newInstance() as BooleanExpr
        boolCache[text] = SoftReference(instance)

        return { q -> instance.compute(q) }
    }

    companion object Default : Molang()
}