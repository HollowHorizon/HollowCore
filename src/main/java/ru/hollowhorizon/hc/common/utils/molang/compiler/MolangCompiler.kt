package ru.hollowhorizon.hc.common.utils.molang.compiler

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.LOGGER
import ru.hollowhorizon.hc.common.utils.molang.lexer.Lexer
import ru.hollowhorizon.hc.common.utils.molang.lexer.Token
import ru.hollowhorizon.hc.common.utils.molang.parser.*
import ru.hollowhorizon.hc.common.utils.molang.runtime.Query
import ru.hollowhorizon.hc.common.utils.molang.runtime.Variables
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

fun main() {
    MolangCompiler.compile(Parser(Lexer("q.anim_time * 8 + v.test_value.x").tokenize()).parseFloatExpr())
}

fun interface FloatExpr {
    operator fun invoke(query: Query, variables: Variables): Float
}

object MolangCompiler {
    fun compile(ast: AstFloat): FloatExpr {
        val optimized = optimizeConstants(ast)
        return if (optimized is NumberLiteral) FloatExpr { _, _ -> optimized.value }
        else codegenFloat(optimized)
    }

    private fun codegenFloat(ast: AstFloat): FloatExpr {
        val className = "GeneratedFloatExpr_${ast.hashCode().toUInt()}"
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
            "invoke",
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

        File("test.class").apply {
            writeBytes(bytecode)
            LOGGER.info("Generated at $absolutePath.")
        }

        TODO()
    }

    private fun generateFloatExpression(mv: MethodVisitor, ast: AstFloat) {
        when (ast) {
            is NumberLiteral -> mv.visitLdcInsn(ast.value)
            is VariableAccess -> {
                if (ast.field != null && (ast.base == "q" || ast.base == "query")) {
                    mv.visitVarInsn(ALOAD, 1)
                    val property = Query::class.memberProperties.find { it.name == ast.field }
                        ?: error("Function ${ast.field} not found!")

                    val methodName = property.javaGetter?.name ?: error("Method for ${property.name} not found!")
                    val descriptor = Type.getMethodDescriptor(property.javaGetter)

                    mv.visitMethodInsn(
                        INVOKEINTERFACE,
                        QUERY.internalName,
                        methodName,
                        descriptor,
                        true
                    )

                    if (property.returnType.classifier == Boolean::class) {
                        val trueLabel = Label()
                        val endLabel = Label()
                        mv.visitJumpInsn(IFNE, trueLabel)
                        mv.visitInsn(FCONST_0)
                        mv.visitJumpInsn(GOTO, endLabel)
                        mv.visitLabel(trueLabel)
                        mv.visitInsn(FCONST_1)
                        mv.visitLabel(endLabel)
                    }
                } else {
                    mv.visitVarInsn(ALOAD, 2)
                    mv.visitLdcInsn(ast.base)
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        VARIABLES.internalName,
                        "get",
                        "(Ljava/lang/String;)F",
                        false
                    )
                }
            }

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
                val resolved = MolangFuntions.resolve(ast.name, ast.args)
                generateFloatExpression(mv, resolved)
            }

            is RuntimeCall -> {
                TODO()
            }
        }
    }

    private fun generateBooleanExpression(mv: MethodVisitor, ast: AstBoolean) {
        when (ast) {
            is BoolLiteral -> mv.visitInsn(if (ast.value) ICONST_1 else ICONST_0)
            is CompareOp -> {
                generateFloatExpression(mv, ast.left)
                generateFloatExpression(mv, ast.right)
                val trueLabel = Label()
                val endLabel = Label()

                when (ast.op) {
                    Token.Type.EQ -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFEQ, trueLabel) }
                    Token.Type.NEQ -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFNE, trueLabel) }
                    Token.Type.LT -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFLT, trueLabel) }
                    Token.Type.GT -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFGT, trueLabel) }
                    Token.Type.LTE -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFLE, trueLabel) }
                    Token.Type.GTE -> { mv.visitInsn(FCMPL); mv.visitJumpInsn(IFGE, trueLabel) }
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
                val trueLabel = Label()
                val endLabel = Label()
                generateFloatExpression(mv, ast.expr)
                mv.visitInsn(FCONST_0)
                mv.visitInsn(FCMPL)
                mv.visitJumpInsn(IFNE, trueLabel)
                mv.visitInsn(ICONST_0)
                mv.visitJumpInsn(GOTO, endLabel)
                mv.visitLabel(trueLabel)
                mv.visitInsn(ICONST_1)
                mv.visitLabel(endLabel)
            }
        }
    }

    private fun getQueryFieldDescriptor(fieldName: String): String {
        return try {
            val prop = Query::class.memberProperties.first { it.name == fieldName }
            if (prop.returnType.classifier == Boolean::class) "Z" else "F"
        } catch (e: Exception) {
            ""
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