package ru.hollowhorizon.hc.common.utils.molang.compiler

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonPrimitive
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.common.utils.molang.lexer.Lexer
import ru.hollowhorizon.hc.common.utils.molang.lexer.Token
import ru.hollowhorizon.hc.common.utils.molang.parser.*
import ru.hollowhorizon.hc.common.utils.molang.runtime.Query
import ru.hollowhorizon.hc.common.utils.molang.runtime.Variables
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.io.File
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

fun JsonPrimitive.parseMolangExpression() = MolangExpression(content)

@Serializable
class MolangVec3(
    private val expressionX: MolangExpression,
    private val expressionY: MolangExpression,
    private val expressionZ: MolangExpression,
) {

    fun getX(query: Query, variables: Variables) = expressionX.getFloat(query, variables)
    fun getY(query: Query, variables: Variables) = expressionY.getFloat(query, variables)
    fun getZ(query: Query, variables: Variables) = expressionZ.getFloat(query, variables)
}

@Serializable
class MolangExpression(private val expression: String) : FloatExpr {
    @Transient
    private val compiled = MolangCompiler.compileFloat(expression)

    override fun getFloat(query: Query, variables: Variables): Float {
        return compiled.getFloat(query, variables)
    }
}

fun interface FloatExpr {
    fun getFloat(query: Query, variables: Variables): Float
}

fun interface BoolExpr {
    fun getBoolean(query: Query, variables: Variables): Boolean
}

object MolangCompiler {
    private class BytecodeClassLoader : ClassLoader(Thread.currentThread().contextClassLoader) {
        fun defineClass(name: String, bytecode: ByteArray): Class<*> {
            val loaded = findLoadedClass(name)
            if (loaded != null) return loaded

            try {
                return super.defineClass(name, bytecode, 0, bytecode.size)
            } catch (e: Throwable) {
                println("Error loading class $name: ${e.message}")
                throw e
            }

        }
    }

    private val classLoader = BytecodeClassLoader()

    private var generatedIndex = 0

    private val floatFunctions = Object2ObjectOpenHashMap<String, FloatExpr>()
    private val boolFunctions = Object2ObjectOpenHashMap<String, BoolExpr>()

    fun compileBoolean(expression: String) = boolFunctions.getOrPut(expression) {
        compile(Parser(Lexer(expression).tokenize()).parseBooleanExpr())
    }

    fun compileFloat(expression: String) = floatFunctions.getOrPut(expression) {
        compile(Parser(Lexer(expression).tokenize()).parseFloatExpr())
    }

    private fun compile(ast: AstBoolean): BoolExpr {
        return ast as? BoolLiteral ?: codegenBoolean(ast)
    }

    private fun compile(ast: AstFloat): FloatExpr {
        return ast as? NumberLiteral ?: codegenFloat(ast)
    }

    private fun codegenBoolean(ast: AstBoolean): BoolExpr {
        val className = "GeneratedBooleanExpr${generatedIndex++}"
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(
            V17,
            ACC_PUBLIC or ACC_FINAL,
            className,
            null,
            "java/lang/Object",
            arrayOf("ru/hollowhorizon/hc/common/utils/molang/compiler/BoolExpr")
        )
        generateCtor(cw)
        val mv = cw.visitMethod(
            ACC_PUBLIC,
            "getBoolean",
            "(${QUERY.descriptor}${VARIABLES.descriptor})Z",
            null,
            null
        )
        mv.visitCode()
        generateBooleanExpression(mv, ast)
        mv.visitInsn(IRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        cw.visitEnd()

        val bytecode = cw.toByteArray()

        val clazz = classLoader.defineClass(className, bytecode)
        val instance = clazz.getDeclaredConstructor().newInstance()
        return instance as BoolExpr
    }

    private fun codegenFloat(ast: AstFloat): FloatExpr {
        val className = "GeneratedFloatExpr${generatedIndex++}"
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(
            V17,
            ACC_PUBLIC or ACC_FINAL,
            className,
            null,
            "java/lang/Object",
            arrayOf("ru/hollowhorizon/hc/common/utils/molang/compiler/FloatExpr")
        )
        generateCtor(cw)
        val mv = cw.visitMethod(
            ACC_PUBLIC,
            "getFloat",
            "(${QUERY.descriptor}${VARIABLES.descriptor})F",
            null,
            null
        )
        mv.visitCode()
        generateFloatExpression(mv, ast)
        mv.visitInsn(FRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        cw.visitEnd()

        val bytecode = cw.toByteArray()


        val clazz = classLoader.defineClass(className, bytecode)
        val instance = clazz.getDeclaredConstructor().newInstance()
        return instance as FloatExpr
    }

    private fun generateFloatExpression(mv: MethodVisitor, ast: AstFloat) {
        when (ast) {
            is NumberLiteral -> mv.visitLdcInsn(ast.value)
            is VariableAccess -> generateVariableAccess(ast, mv, false)

            is BinaryOp -> {
                generateFloatExpression(mv, ast.left)
                generateFloatExpression(mv, ast.right)
                val opcode = when (ast.op) {
                    Token.Type.ADD -> FADD
                    Token.Type.SUB -> FSUB
                    Token.Type.MUL -> FMUL
                    Token.Type.DIV -> FDIV
                    Token.Type.MOD -> FREM
                    else -> error("Unsupported binary operation: ${ast.op}")
                }
                mv.visitInsn(opcode)
            }

            is Conditional -> {
                val elseLabel = Label()
                val endLabel = Label()
                generateBooleanExpression(mv, ast.condition)
                mv.visitJumpInsn(IFEQ, elseLabel)
                generateFloatExpression(mv, ast.thenBranch)
                mv.visitJumpInsn(GOTO, endLabel)
                mv.visitLabel(elseLabel)
                generateFloatExpression(mv, ast.elseBranch)
                mv.visitLabel(endLabel)
            }

            is FunctionCall -> {
                val descriptor = MolangFunctions.resolve(ast.name, ast.args.size)

                ast.args.forEach {
                    generateFloatExpression(mv, it)
                }

                val opcode =
                    if (descriptor.className.startsWith("java/") || descriptor.isStatic) INVOKESTATIC
                    else INVOKEVIRTUAL

                mv.visitMethodInsn(
                    opcode,
                    descriptor.className,
                    descriptor.methodName,
                    Type.getMethodDescriptor(
                        Type.getType(descriptor.returnType),
                        *(0..<descriptor.argCount).map { Type.getType(Float::class.java) }.toTypedArray()
                    ),
                    false
                )
            }
        }
    }

    private fun generateVariableAccess(
        ast: VariableAccess,
        mv: MethodVisitor,
        isBoolean: Boolean,
    ) {
        val path = ast.path
        if (path.isNotEmpty() && (path[0] == "q" || path[0] == "query")) {
            mv.visitVarInsn(ALOAD, 1)
            var currentClass: KClass<*> = Query::class

            for (i in 1 until path.size) {
                val propName = path[i]
                val property = currentClass.memberProperties.find { it.name == propName }
                    ?: error("Property $propName not found in ${currentClass.simpleName}!")

                val getter = property.javaGetter
                    ?: error("Property '$propName' has no getter in ${currentClass.simpleName}")
                val declaringClass = getter.declaringClass


                mv.visitMethodInsn(
                    when {
                        declaringClass.isInterface -> INVOKEINTERFACE
                        Modifier.isStatic(getter.modifiers) -> INVOKESTATIC
                        else -> INVOKEVIRTUAL
                    },
                    Type.getInternalName(getter.declaringClass),
                    getter.name,
                    Type.getMethodDescriptor(getter),
                    declaringClass.isInterface
                )

                currentClass = property.returnType.classifier as KClass<*>
            }

            if (currentClass == Boolean::class) {
                if (!isBoolean) convertBooleanToFloat(mv)
            } else if (currentClass == Float::class) {
                if (isBoolean) convertFloatToBoolean(mv)
            } else if (currentClass != Float::class) {
                error("Unsupported return type: $currentClass")
            }
        } else {
            mv.visitVarInsn(ALOAD, 2)
            mv.visitLdcInsn(ast.path.joinToString("."))
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                VARIABLES.internalName,
                "get",
                "(Ljava/lang/String;)F",
                false
            )

            if (isBoolean) {
                convertFloatToBoolean(mv)
            }
        }
    }

    private fun convertBooleanToFloat(mv: MethodVisitor) {
        val trueLabel = Label()
        val endLabel = Label()
        mv.visitJumpInsn(IFNE, trueLabel)
        mv.visitInsn(FCONST_0)
        mv.visitJumpInsn(GOTO, endLabel)
        mv.visitLabel(trueLabel)
        mv.visitInsn(FCONST_1)
        mv.visitLabel(endLabel)
    }

    private fun convertFloatToBoolean(mv: MethodVisitor) {
        val trueLabel = Label()
        val endLabel = Label()

        mv.visitInsn(FCONST_0)
        mv.visitInsn(FCMPL)
        mv.visitJumpInsn(IFNE, trueLabel)
        mv.visitInsn(ICONST_0)
        mv.visitJumpInsn(GOTO, endLabel)
        mv.visitLabel(trueLabel)
        mv.visitInsn(ICONST_1)
        mv.visitLabel(endLabel)
    }

    fun findPropertyRecursive(name: String, clazz: Class<*>): PropertyDescriptor? {
        for (pd in Introspector.getBeanInfo(clazz).propertyDescriptors) {
            if (pd.name == name) return pd
        }
        return null
    }

    private fun generateBooleanExpression(mv: MethodVisitor, ast: AstBoolean) {
        when (ast) {
            is BoolLiteral -> mv.visitInsn(if (ast.value) ICONST_1 else ICONST_0)
            is VariableAccess -> generateVariableAccess(ast, mv, true)
            is CompareOp -> {
                generateFloatExpression(mv, ast.left)
                generateFloatExpression(mv, ast.right)
                val trueLabel = Label()
                val endLabel = Label()

                when (ast.op) {
                    Token.Type.EQ -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFEQ, trueLabel)
                    }

                    Token.Type.NEQ -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFNE, trueLabel)
                    }

                    Token.Type.LT -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFLT, trueLabel)
                    }

                    Token.Type.GT -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFGT, trueLabel)
                    }

                    Token.Type.LTE -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFLE, trueLabel)
                    }

                    Token.Type.GTE -> {
                        mv.visitInsn(FCMPL); mv.visitJumpInsn(IFGE, trueLabel)
                    }

                    else -> error("Unsupported comparison operator: ${ast.op}")
                }

                mv.visitInsn(ICONST_0)
                mv.visitJumpInsn(GOTO, endLabel)
                mv.visitLabel(trueLabel)
                mv.visitInsn(ICONST_1)
                mv.visitLabel(endLabel)
            }

            is LogicalOp -> {
                when (ast.op) {
                    Token.Type.AND -> {
                        val shortCircuitLabel = Label()
                        val endLabel = Label()
                        generateBooleanExpression(mv, ast.left)
                        mv.visitInsn(DUP)
                        mv.visitJumpInsn(IFEQ, shortCircuitLabel)
                        mv.visitInsn(POP)
                        generateBooleanExpression(mv, ast.right)
                        mv.visitJumpInsn(GOTO, endLabel)
                        mv.visitLabel(shortCircuitLabel)
                        mv.visitLabel(endLabel)
                    }

                    Token.Type.OR -> {
                        val shortCircuitLabel = Label()
                        val endLabel = Label()
                        generateBooleanExpression(mv, ast.left)
                        mv.visitInsn(DUP)
                        mv.visitJumpInsn(IFNE, shortCircuitLabel)
                        mv.visitInsn(POP)
                        generateBooleanExpression(mv, ast.right)
                        mv.visitJumpInsn(GOTO, endLabel)
                        mv.visitLabel(shortCircuitLabel)
                        mv.visitLabel(endLabel)
                    }

                    else -> error("Unsupported logical operator: ${ast.op}")
                }
            }

            is NotOp -> {
                val trueLabel = Label()
                val endLabel = Label()
                generateBooleanExpression(mv, ast.expr)
                mv.visitJumpInsn(IFEQ, trueLabel)
                mv.visitInsn(ICONST_0)
                mv.visitJumpInsn(GOTO, endLabel)
                mv.visitLabel(trueLabel)
                mv.visitInsn(ICONST_1)
                mv.visitLabel(endLabel)
            }

            is FloatToBool -> {
                generateFloatExpression(mv, ast.expr)
                convertFloatToBoolean(mv)
            }
        }
    }

    private fun generateCtor(cw: ClassWriter) {
        val ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        ctor.visitCode()
        ctor.visitVarInsn(ALOAD, 0)
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        ctor.visitInsn(RETURN)
        ctor.visitMaxs(0, 0)
        ctor.visitEnd()
    }

    private val QUERY = Type.getType(Query::class.java)
    private val VARIABLES = Type.getType(Variables::class.java)
}