package ru.hollowhorizon.hc.common.utils.molang.compiler

import ru.hollowhorizon.hc.common.utils.molang.lexer.Token
import ru.hollowhorizon.hc.common.utils.molang.parser.*

internal fun optimizeConstants(ast: AstFloat): AstFloat {
    return when (ast) {
        is NumberLiteral, is VariableAccess -> ast
        is BinaryOp -> {
            val left = optimizeConstants(ast.left)
            val right = optimizeConstants(ast.right)

            if (left is NumberLiteral && right is NumberLiteral) {
                when (ast.op) {
                    Token.Type.ADD -> NumberLiteral(left.value + right.value)
                    Token.Type.SUB -> NumberLiteral(left.value - right.value)
                    Token.Type.MUL -> NumberLiteral(left.value * right.value)
                    Token.Type.DIV -> if (right.value != 0f) NumberLiteral(left.value / right.value) else error("Division by zero!")
                    Token.Type.MOD -> if (right.value != 0f) NumberLiteral(left.value % right.value) else error("Division by zero!")
                    else -> error("Unsupported operation ${ast.op}!")
                }
            } else {
                BinaryOp(left, ast.op, right)
            }
        }

        is FunctionCall -> optimizeConstants(MolangFuntions.resolve(ast.name, ast.args))
        is Conditional -> {
            val condition = optimizeBooleanConstants(ast.condition)

            if (condition is BoolLiteral) {
                if (condition.value) optimizeConstants(ast.thenBranch)
                else optimizeConstants(ast.elseBranch)
            } else {
                Conditional(
                    condition,
                    optimizeConstants(ast.thenBranch),
                    optimizeConstants(ast.elseBranch)
                )
            }
        }

        is RuntimeCall -> {
            val optimizedArgs = ast.args.map(::optimizeConstants)
            if (optimizedArgs.all { it is NumberLiteral }) {
                try {
                    val values = optimizedArgs.map { (it as NumberLiteral).value }
                    NumberLiteral(ast.impl(values))
                } catch (e: Exception) {
                    RuntimeCall(ast.name, optimizedArgs, ast.impl)
                }
            } else {
                RuntimeCall(ast.name, optimizedArgs, ast.impl)
            }
        }
    }
}

internal fun optimizeBooleanConstants(ast: AstBoolean): AstBoolean {
    return when (ast) {
        is BoolLiteral -> ast
        is CompareOp -> {
            val left = optimizeConstants(ast.left)
            val right = optimizeConstants(ast.right)

            if (left is NumberLiteral && right is NumberLiteral) {
                val result = when (ast.op) {
                    Token.Type.EQ -> left.value == right.value
                    Token.Type.NEQ -> left.value != right.value
                    Token.Type.LT -> left.value < right.value
                    Token.Type.GT -> left.value > right.value
                    Token.Type.LTE -> left.value <= right.value
                    Token.Type.GTE -> left.value >= right.value
                    else -> return CompareOp(left, ast.op, right)
                }
                BoolLiteral(result)
            } else {
                CompareOp(left, ast.op, right)
            }
        }

        is LogicalOp -> when (ast.op) {
            Token.Type.AND -> optimizeAnd(ast)
            Token.Type.OR -> optimizeOr(ast)
            else -> LogicalOp(optimizeBooleanConstants(ast.left), ast.op, optimizeBooleanConstants(ast.right))
        }


        is NotOp -> {
            val expr = optimizeBooleanConstants(ast.expr)
            if (expr is BoolLiteral) BoolLiteral(!expr.value) else NotOp(expr)
        }

        is FloatToBool -> {
            val expr = optimizeConstants(ast.expr)
            if (expr is NumberLiteral) BoolLiteral(expr.value != 0f) else FloatToBool(expr)
        }
    }
}

private fun optimizeAnd(ast: LogicalOp): AstBoolean {
    val left = optimizeBooleanConstants(ast.left)
    if (left is BoolLiteral) {
        return if (!left.value) BoolLiteral(false) else optimizeBooleanConstants(ast.right)
    }
    val right = optimizeBooleanConstants(ast.right)
    if (right is BoolLiteral) {
        return if (!right.value) BoolLiteral(false) else left
    }
    return LogicalOp(left, ast.op, right)
}

private fun optimizeOr(ast: LogicalOp): AstBoolean {
    val left = optimizeBooleanConstants(ast.left)
    if (left is BoolLiteral) {
        return if (left.value) BoolLiteral(true) else optimizeBooleanConstants(ast.right)
    }
    val right = optimizeBooleanConstants(ast.right)
    if (right is BoolLiteral) {
        return if (right.value) BoolLiteral(true) else left
    }
    return LogicalOp(left, ast.op, right)
}