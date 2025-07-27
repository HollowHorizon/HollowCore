package ru.hollowhorizon.hc.common.utils.molang.parser

import ru.hollowhorizon.hc.common.utils.molang.lexer.Token

sealed interface AstNode
sealed interface AstFloat : AstNode
sealed interface AstBoolean : AstNode

data class NumberLiteral(val value: Float) : AstFloat
data class VariableAccess(val base: String, val field: String? = null) : AstFloat
data class BinaryOp(val left: AstFloat, val op: Token.Type, val right: AstFloat) : AstFloat
data class FunctionCall(val name: String, val args: List<AstFloat>) : AstFloat
data class Conditional(val condition: AstBoolean, val thenBranch: AstFloat, val elseBranch: AstFloat) : AstFloat
// TODO: May be use invoke dynamic or something like that?
data class RuntimeCall(val name: String, val args: List<AstFloat>, val impl: (List<Float>) -> Float) : AstFloat
data class BoolLiteral(val value: Boolean) : AstBoolean
data class CompareOp(val left: AstFloat, val op: Token.Type, val right: AstFloat) : AstBoolean
data class LogicalOp(val left: AstBoolean, val op: Token.Type, val right: AstBoolean) : AstBoolean
data class NotOp(val expr: AstBoolean) : AstBoolean
data class FloatToBool(val expr: AstFloat) : AstBoolean