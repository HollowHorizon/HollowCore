package ru.hollowhorizon.hc.common.utils.molang.parser

import ru.hollowhorizon.hc.common.utils.molang.lexer.Token

class Parser(private val tokens: List<Token>) {
    private var pos = 0
    private fun peek(offset: Int = 0) = tokens.getOrNull(pos + offset)
    private fun current() = tokens.getOrNull(pos)
    private fun advance() = tokens[pos++]
    private fun match(vararg types: Token.Type): Boolean {
        val tok = current() ?: return false
        return if (tok.type in types) { pos++; true } else false
    }
    private fun expect(type: Token.Type): Token {
        val tok = advance()
        if (tok.type != type)
            error("Expected $type but got ${tok.type} at position ${tok.position}")
        return tok
    }

    fun parseFloatExpr(): AstFloat = parseFloatConditional()

    private fun parseFloatConditional(): AstFloat {
        val expr = parseFloatLogicalOr()
        return if (match(Token.Type.QUESTION)) {
            val thenExpr = parseFloatExpr()
            expect(Token.Type.COLON)
            val elseExpr = parseFloatExpr()
            Conditional(FloatToBool(expr), thenExpr, elseExpr)
        } else {
            expr
        }
    }

    private fun parseFloatLogicalOr(): AstFloat {
        var expr = parseFloatLogicalAnd()
        while (match(Token.Type.OR)) {
            val right = parseFloatLogicalAnd()
            val leftBool = FloatToBool(expr)
            val rightBool = FloatToBool(right)
            val logicalResult = LogicalOp(leftBool, Token.Type.OR, rightBool)
            expr = Conditional(logicalResult, NumberLiteral(1f), NumberLiteral(0f))
        }
        return expr
    }

    private fun parseFloatLogicalAnd(): AstFloat {
        var expr = parseFloatComparison()
        while (match(Token.Type.AND)) {
            val right = parseFloatComparison()
            val leftBool = FloatToBool(expr)
            val rightBool = FloatToBool(right)
            val logicalResult = LogicalOp(leftBool, Token.Type.AND, rightBool)
            expr = Conditional(logicalResult, NumberLiteral(1f), NumberLiteral(0f))
        }
        return expr
    }

    private fun parseFloatComparison(): AstFloat {
        var expr = parseFloatAddSub()
        while (true) {
            when (val op = peek()?.type) {
                Token.Type.EQ, Token.Type.NEQ, Token.Type.LT,
                Token.Type.GT, Token.Type.LTE, Token.Type.GTE -> {
                    advance()
                    val right = parseFloatAddSub()
                    val compareResult = CompareOp(expr, op, right)
                    expr = Conditional(compareResult, NumberLiteral(1f), NumberLiteral(0f))
                }
                else -> break
            }
        }
        return expr
    }

    private fun parseFloatAddSub(): AstFloat {
        var expr = parseFloatMulDivMod()
        while (true) {
            when (val op = peek()?.type) {
                Token.Type.ADD, Token.Type.SUB -> {
                    advance()
                    val right = parseFloatMulDivMod()
                    expr = BinaryOp(expr, op, right)
                }
                else -> break
            }
        }
        return expr
    }

    private fun parseFloatMulDivMod(): AstFloat {
        var expr = parseFloatUnary()
        while (true) {
            when (val op = peek()?.type) {
                Token.Type.MUL, Token.Type.DIV, Token.Type.MOD -> {
                    advance()
                    val right = parseFloatUnary()
                    expr = BinaryOp(expr, op, right)
                }
                else -> break
            }
        }
        return expr
    }


    private fun parseFloatUnary(): AstFloat {
        return when {
            match(Token.Type.SUB) -> {
                val expr = parseFloatUnary()
                BinaryOp(NumberLiteral(0f), Token.Type.SUB, expr)
            }
            match(Token.Type.NOT) -> {
                val expr = parseFloatUnary()
                val notResult = NotOp(FloatToBool(expr))
                Conditional(notResult, NumberLiteral(1f), NumberLiteral(0f))
            }
            else -> parsePrimaryFloat()
        }
    }

    private fun parsePrimaryFloat(): AstFloat {
        val tok = current() ?: error("Unexpected EOF in float expression")
        return when (tok.type) {
            Token.Type.NUMBER -> {
                advance()
                NumberLiteral(tok.value.toFloat())
            }
            Token.Type.BOOLEAN -> {
                advance()
                val value = if (tok.value == "true") 1f else 0f
                NumberLiteral(value)
            }
            Token.Type.IDENTIFIER -> {
                advance()
                parseIdentifierOrFunction(tok)
            }
            Token.Type.LPAREN -> {
                advance()
                val expr = parseFloatExpr()
                expect(Token.Type.RPAREN)
                expr
            }
            else -> error("Unexpected token ${tok.type} in float expression")
        }
    }

    private fun parseIdentifierOrFunction(tok: Token): AstFloat {
        return when (peek()?.type) {
            Token.Type.DOT -> {
                advance()
                val field = expect(Token.Type.IDENTIFIER).value
                VariableAccess(tok.value, field)
            }
            Token.Type.LPAREN -> {
                advance()
                val args = mutableListOf<AstFloat>()
                if (peek()?.type != Token.Type.RPAREN) {
                    do {
                        args += parseFloatExpr()
                    } while (match(Token.Type.COMMA))
                }
                expect(Token.Type.RPAREN)
                FunctionCall(tok.value, args)
            }
            else -> VariableAccess(tok.value, null)
        }
    }

    fun parseBooleanExpr(): AstBoolean = parseLogicalOr()

    private fun parseLogicalOr(): AstBoolean {
        var expr = parseLogicalAnd()
        while (match(Token.Type.OR)) {
            val right = parseLogicalAnd()
            expr = LogicalOp(expr, Token.Type.OR, right)
        }
        return expr
    }

    private fun parseLogicalAnd(): AstBoolean {
        var expr = parseUnaryBoolean()
        while (match(Token.Type.AND)) {
            val right = parseUnaryBoolean()
            expr = LogicalOp(expr, Token.Type.AND, right)
        }
        return expr
    }

    private fun parseUnaryBoolean(): AstBoolean {
        return when {
            match(Token.Type.NOT) -> NotOp(parseUnaryBoolean())
            peek()?.type == Token.Type.LPAREN -> {
                advance()
                val inner = parseBooleanExpr()
                expect(Token.Type.RPAREN)
                inner
            }
            else -> parseComparison()
        }
    }

    private fun parseComparison(): AstBoolean {
        val left = parseBooleanAddSub()
        return when {
            match(Token.Type.EQ)  -> CompareOp(left, Token.Type.EQ, parseBooleanAddSub())
            match(Token.Type.NEQ) -> CompareOp(left, Token.Type.NEQ, parseBooleanAddSub())
            match(Token.Type.LT)  -> CompareOp(left, Token.Type.LT, parseBooleanAddSub())
            match(Token.Type.GT)  -> CompareOp(left, Token.Type.GT, parseBooleanAddSub())
            match(Token.Type.LTE) -> CompareOp(left, Token.Type.LTE, parseBooleanAddSub())
            match(Token.Type.GTE) -> CompareOp(left, Token.Type.GTE, parseBooleanAddSub())
            else -> FloatToBool(left)
        }
    }

    private fun parseBooleanAddSub(): AstFloat {
        var expr = parseBooleanMulDivMod()
        while (true) {
            when (val op = peek()?.type) {
                Token.Type.ADD, Token.Type.SUB -> {
                    advance()
                    val right = parseBooleanMulDivMod()
                    expr = BinaryOp(expr, op, right)
                }
                else -> break
            }
        }
        return expr
    }

    private fun parseBooleanMulDivMod(): AstFloat {
        var expr = parseBooleanUnary()
        while (true) {
            when (val op = peek()?.type) {
                Token.Type.MUL, Token.Type.DIV, Token.Type.MOD -> {
                    advance()
                    val right = parseBooleanUnary()
                    expr = BinaryOp(expr, op, right)
                }
                else -> break
            }
        }
        return expr
    }

    private fun parseBooleanUnary(): AstFloat {
        return when {
            match(Token.Type.SUB) -> {
                val expr = parseBooleanUnary()
                BinaryOp(NumberLiteral(0f), Token.Type.SUB, expr)
            }
            else -> parsePrimaryBoolean()
        }
    }

    private fun parsePrimaryBoolean(): AstFloat {
        val tok = current() ?: error("Unexpected EOF in float expression")
        return when (tok.type) {
            Token.Type.NUMBER -> {
                advance()
                NumberLiteral(tok.value.toFloat())
            }
            Token.Type.BOOLEAN -> {
                advance()
                val value = if (tok.value == "true") 1f else 0f
                NumberLiteral(value)
            }
            Token.Type.IDENTIFIER -> {
                advance()
                parseBooleanIdentifierOrFunction(tok)
            }
            Token.Type.LPAREN -> {
                advance()
                val expr = parseBooleanAddSub()
                expect(Token.Type.RPAREN)
                expr
            }
            else -> error("Unexpected token ${tok.type} in float expression")
        }
    }

    private fun parseBooleanIdentifierOrFunction(tok: Token): AstFloat {
        return when (peek()?.type) {
            Token.Type.DOT -> {
                advance()
                val field = expect(Token.Type.IDENTIFIER).value
                VariableAccess(tok.value, field)
            }
            Token.Type.LPAREN -> {
                advance()
                val args = mutableListOf<AstFloat>()
                if (peek()?.type != Token.Type.RPAREN) {
                    do {
                        args += parseBooleanAddSub()
                    } while (match(Token.Type.COMMA))
                }
                expect(Token.Type.RPAREN)
                FunctionCall(tok.value, args)
            }
            else -> VariableAccess(tok.value, null)
        }
    }
}