package ru.hollowhorizon.hc.common.utils.molang

import de.fabmax.kool.math.QuatF
import de.fabmax.kool.math.Vec3f
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

fun interface QuatFExpr {
    fun compute(q: EntityQuery): QuatF
}

fun interface Vec3fExpr {
    fun compute(q: EntityQuery): Vec3f
}

object MolangCompilerScope : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob())

open class Molang(private val parent: ClassLoader? = null) {
    private val floatCache = mutableMapOf<String, SoftReference<FloatExpr>>()
    private val boolCache = mutableMapOf<String, SoftReference<BooleanExpr>>()
    private val quatFCache = mutableMapOf<String, SoftReference<QuatFExpr>>()
    private val vec3fCache = mutableMapOf<String, SoftReference<Vec3fExpr>>()

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

    fun compileQuatF(text: String): (EntityQuery) -> QuatF {
        quatFCache[text]?.get()?.let { cached ->
            return { q -> cached.compute(q) }
        }

        val safeHash = text.hashCode().toUInt()
        val className = "QuatFExpr_$safeHash"

        val cbe = ClassBodyEvaluator().apply {
            setImplementedInterfaces(arrayOf(QuatFExpr::class.java))
            setClassName(className)
            setParentClassLoader(parent ?: this@Molang.javaClass.classLoader)
            setDefaultImports(
                "java.lang.*",
                "ru.hollowhorizon.hc.common.utils.molang.*",
                "de.fabmax.kool.math.QuatF",
                "static java.lang.Math.*"
            )
        }

        val source = """
            public QuatF compute(${EntityQuery::class.java.name} q) {
                return ($text);
            }
        """.trimIndent()
        cbe.cook(source)

        val instance = cbe.clazz.getDeclaredConstructor().newInstance() as QuatFExpr
        quatFCache[text] = SoftReference(instance)

        return { q -> instance.compute(q) }
    }

    fun compileVec3f(text: String): (EntityQuery) -> Vec3f {
        vec3fCache[text]?.get()?.let { cached ->
            return { q -> cached.compute(q) }
        }

        val safeHash = text.hashCode().toUInt()
        val className = "Vec3fExpr_$safeHash"

        val cbe = ClassBodyEvaluator().apply {
            setImplementedInterfaces(arrayOf(Vec3fExpr::class.java))
            setClassName(className)
            setParentClassLoader(parent ?: this@Molang.javaClass.classLoader)
            setDefaultImports(
                "java.lang.*",
                "ru.hollowhorizon.hc.common.utils.molang.*",
                "de.fabmax.kool.math.Vec3f",
                "static java.lang.Math.*"
            )
        }

        val source = """
            public Vec3f compute(${EntityQuery::class.java.name} q) {
                return ($text);
            }
        """.trimIndent()
        cbe.cook(source)

        val instance = cbe.clazz.getDeclaredConstructor().newInstance() as Vec3fExpr
        vec3fCache[text] = SoftReference(instance)

        return { q -> instance.compute(q) }
    }

    companion object Default : Molang()
}