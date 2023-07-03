package ru.hollowhorizon.hc.common.scripting.mappings


import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifier
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.*
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.loadAsNBT
import ru.hollowhorizon.hc.mixin.kotlin.LazyJavaClassMemberScopeAccessor

object HollowMappings {
    val MAPPINGS = NBTFormat.deserialize<Mappings>(
        (HollowMappings.javaClass.getResourceAsStream("/mappings.nbt")
            ?: throw IllegalStateException("Mappings file not found!"))
            .loadAsNBT()
    )
}

private val PRIMITIVE_DESC_MAP: HashMap<Char, String> = hashMapOf(
    'I' to "java.lang.Integer",
    'J' to "java.lang.Long",
    'F' to "java.lang.Float",
    'D' to "java.lang.Double",
    'B' to "java.lang.Byte",
    'S' to "java.lang.Short",
    'C' to "java.lang.Character",
    'Z' to "java.lang.Boolean"
)

@Serializable
data class Mappings(val mappings: HashSet<ClassMapping>) {
    operator fun get(callerClass: JavaClassifier?): ClassMapping? {
        if (callerClass !is JavaClass) return null

        val mapping = mappings.find { it.mcpName == callerClass.fqName!!.asString() }

        if (mapping == null) {
            callerClass.supertypes.filter { it.classifier != null }.forEach {
                val result = get(it.classifier!!)
                if (result != null) return result
            }
            return null
        }

        return mapping
    }
}

@Serializable
data class ClassMapping(
    val mcpName: String,
    val srgName: String
) {
    fun fieldObf(text: String): String {
        return fields.find { it.mcpName == text }?.srgName ?: text
    }

    fun fieldDeobf(text: String): String {
        return fields.find { it.srgName == text }?.mcpName ?: text
    }

    fun method(text: String): String {
        return methods.find { it.mcpName == text }?.srgName ?: text
    }

    fun methodObf(deobfName: String, signature: String): String {
        return methods.firstOrNull { it.mcpName == deobfName && it.params == signature }?.srgName ?: deobfName
    }

    fun methodsObf(deobfName: String): List<String> {
        return methods.filter { it.mcpName == deobfName }.map { it.srgName }
    }

    val fields = HashSet<FieldMapping>()
    val methods = HashSet<MethodMapping>()
}

@Serializable
data class FieldMapping(
    val mcpName: String,
    val srgName: String
)

@Serializable
data class MethodMapping(
    val mcpName: String,
    val srgName: String,
    val params: String
)

val KotlinType.signature: String
    get() {
        if (this.isPrimitiveNumberOrNullableType() || this.isUnit()) {
            if(KotlinBuiltIns.isArray(this)) {
                return "[${this.arguments[0].type.signature}"
            }
            return if (this.isInt()) "I"
            else if (this.isLong()) "J"
            else if (this.isFloat()) "F"
            else if (this.isDouble()) "D"
            else if (this.isByte()) "B"
            else if (this.isShort()) "S"
            else if (this.isChar()) "C"
            else if (this.isBoolean()) "Z"
            else if (this.isUnit()) "V"
            else throw ClassNotFoundException("Primitive type $this not found!")
        } else {
            val scope = this.memberScope
            if (scope is LazyJavaClassMemberScopeAccessor) {
                return "L${scope.jClass.fqName!!.asString()};"
            } else throw ClassNotFoundException("Type $this not found!")
        }
    }

fun getSignature(params: List<KotlinType>, returnType: KotlinType): String {
    val sign = StringBuilder()

    sign.append("(")
    params.forEach { p ->
        sign.append(p.signature)
    }
    sign.append(")")

    return sign.append(returnType.signature).toString()
}
