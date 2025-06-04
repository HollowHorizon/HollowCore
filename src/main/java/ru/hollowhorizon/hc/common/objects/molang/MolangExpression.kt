package ru.hollowhorizon.hc.common.objects.molang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.util.Mth
import kotlin.math.*
import kotlin.random.Random

@Serializable(MolangSerializer::class)
fun interface MolangExpression {
    fun eval(context: MolangContext): Float

    companion object {
        val ZERO = Literal(0f)
        val ONE = Literal(1f)
    }

    interface Variable {
        fun assign(context: MolangContext, value: Float)
    }

    data class Literal(val value: Float) : MolangExpression {
        override fun eval(context: MolangContext): Float = value
    }

    data class Negative(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = -inner.eval(context)
    }

    data class Invert(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = 1 / inner.eval(context)
    }

    data class Add(val left: MolangExpression, val right: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = left.eval(context) + right.eval(context)
    }

    data class Multiply(val left: MolangExpression, val right: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = left.eval(context) * right.eval(context)
    }

    data class Sin(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = sin(inner.eval(context) * Mth.DEG_TO_RAD)
    }

    data class Cos(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = cos(inner.eval(context) * Mth.DEG_TO_RAD)
    }

    data class Floor(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = floor(inner.eval(context))
    }

    data class Ceil(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = ceil(inner.eval(context))
    }

    data class Round(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = round(inner.eval(context))
    }

    data class Truncate(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = truncate(inner.eval(context))
    }

    data class Abs(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = abs(inner.eval(context))
    }

    data class Clamp(val value: MolangExpression, val min: MolangExpression, val max: MolangExpression) :
        MolangExpression {
        override fun eval(context: MolangContext): Float =
            value.eval(context).coerceIn(min.eval(context), max.eval(context))
    }

    data class Random(val low: MolangExpression, val high: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float {
            val low = low.eval(context)
            val high = high.eval(context)
            val random = (context.query as? MolangQueryRandom)?.random ?: Random
            return random.nextFloat() * (high - low) + low
        }
    }

    data class Query(val f: MolangQuery.() -> Float) : MolangExpression {
        override fun eval(context: MolangContext): Float = context.query.run(f)

        companion object {
            inline operator fun <reified T : MolangQuery> invoke(crossinline f: T.() -> Float) =
                Query { (this as? T)?.let(f) ?: 0f }
        }
    }

    data class Compare(val left: MolangExpression, val right: MolangExpression, val op: Op) : MolangExpression {
        override fun eval(context: MolangContext): Float =
            if (op.check(left.eval(context), right.eval(context))) 1f else 0f

        enum class Op(val check: (Float, Float) -> Boolean) {
            Equal({ x, y -> x == y }), NotEqual({ x, y -> x != y }), Less({ x, y -> x < y }), LessEqual({ x, y -> x <= y }), More(
                { x, y -> x > y }),
            MoreEqual({ x, y -> x >= y }),
        }
    }

    data class Or(val left: MolangExpression, val right: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float =
            if (left.eval(context) != 0f || right.eval(context) != 0f) 1f else 0f
    }

    data class And(val left: MolangExpression, val right: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float =
            if (left.eval(context) != 0f && right.eval(context) != 0f) 1f else 0f
    }

    data class Ternary(
        val condition: MolangExpression,
        val onTrue: MolangExpression,
        val onFalse: MolangExpression,
    ) : MolangExpression {
        override fun eval(context: MolangContext): Float {
            return (if (condition.eval(context) != 0f) onTrue else onFalse).eval(context)
        }
    }

    data class VariableAccess(val key: String) : MolangExpression, Variable {
        override fun eval(context: MolangContext): Float = context.variables[key]
        override fun assign(context: MolangContext, value: Float) {
            context.variables[key] = value
        }
    }

    data class Assignment(val variable: Variable, val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float {
            variable.assign(context, inner.eval(context))
            return 0f
        }
    }

    data class Statements(
        val statements: List<MolangExpression>,
        val result: MolangExpression = ZERO,
    ) :
        MolangExpression {
        override fun eval(context: MolangContext): Float {
            statements.forEach { it.eval(context) }
            return result.eval(context)
        }
    }

    data class Return(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext) = throw Return(inner.eval(context))

        internal class Return(val value: Float) : Throwable()
    }

    data class ComplexExpr(val inner: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float = try {
            inner.eval(context)
        } catch (e: Return.Return) {
            e.value
        }
    }

    class NullCoalescing(val left: MolangExpression, val right: MolangExpression) : MolangExpression {
        override fun eval(context: MolangContext): Float {
            val leftValue = left.eval(context)
            return if (leftValue.isNaN()) right.eval(context) else leftValue
        }
    }
}

@Suppress("ControlFlowWithEmptyBody")
private class Parser(input: String) {
    private var returnsCount: Int = 0
    val normalizedInput = input.lowercase()
    var position = 0

    val currentChar get() = normalizedInput[position]

    fun read(char: Char): Boolean = read { it == char }
    fun read(charRange: CharRange): Boolean = read { it in charRange }

    inline fun read(predicate: (char: Char) -> Boolean): Boolean {
        if (position >= normalizedInput.length || !predicate(currentChar)) return false
        position++
        skipWhitespace()
        return true
    }

    fun read(s: String): Boolean {
        if (normalizedInput.startsWith(s, position)) {
            position += s.length
            skipWhitespace()
            return true
        }
        return false
    }

    fun skipWhitespace() {
        while (position < normalizedInput.length && normalizedInput[position] == ' ') {
            position++
        }
    }

    fun parseLiteral(): MolangExpression.Literal {
        val start = position
        while (read('0'..'9'));
        if (read('.')) {
            while (read('0'..'9'));
        }
        read('f')
        return MolangExpression.Literal(normalizedInput.substring(start, position).filterNot { it.isWhitespace() }
            .toFloat())
    }

    fun parseIdentifier(): String {
        val start = position
        while (read { it.isLetterOrDigit() || it == '_' });
        return normalizedInput.substring(start, position).filterNot { it.isWhitespace() }
    }

    fun parseSimpleExpression(): MolangExpression {
        return when {
            read('(') -> parseExpression().also { read(')') }
            currentChar.isDigit() -> parseLiteral()
            currentChar == '-' -> {
                read('-')
                MolangExpression.Negative(parseSimpleExpression())
            }

            read("math.pi") -> MolangExpression.Literal(PI.toFloat())
            read("math.cos(") -> MolangExpression.Cos(parseExpression()).also { read(')') }
            read("math.sin(") -> MolangExpression.Sin(parseExpression()).also { read(')') }
            read("math.floor(") -> MolangExpression.Floor(parseExpression()).also { read(')') }
            read("math.ceil(") -> MolangExpression.Ceil(parseExpression()).also { read(')') }
            read("math.round(") -> MolangExpression.Round(parseExpression()).also { read(')') }
            read("math.trunc(") -> MolangExpression.Truncate(parseExpression()).also { read(')') }
            read("math.abs(") -> MolangExpression.Abs(parseExpression()).also { read(')') }
            read("math.clamp(") -> MolangExpression.Clamp(
                parseExpression().also { read(',') },
                parseExpression().also { read(',') },
                parseExpression(),
            ).also { read(')') }

            read("math.random(") -> MolangExpression.Random(parseExpression().also { read(',') }, parseExpression())
                .also { read(')') }

            read("query.anim_time") -> MolangExpression.Query<MolangQueryAnimation> { animTime }
            read("query.life_time") -> MolangExpression.Query<MolangQueryEntity> { lifeTime }
            read("query.modified_move_speed") -> MolangExpression.Query<MolangQueryEntity> { modifiedMoveSpeed }
            read("query.modified_distance_moved") -> MolangExpression.Query<MolangQueryEntity> { modifiedDistanceMoved }
            read("variable.") -> MolangExpression.VariableAccess(parseIdentifier())
            else -> throw IllegalArgumentException("Unexpected character at $position")
        }
    }

    fun parseMultiply(): MolangExpression {
        var result = parseSimpleExpression()
        while (true) {
            result = when {
                read('*') -> MolangExpression.Multiply(result, parseSimpleExpression())
                read('/') -> MolangExpression.Multiply(result, MolangExpression.Invert(parseSimpleExpression()))
                else -> return result
            }
        }
    }

    fun parseAdd(): MolangExpression {
        var left = parseMultiply()
        while (true) {
            left = when {
                read('+') -> MolangExpression.Add(left, parseMultiply())
                read('-') -> MolangExpression.Add(left, MolangExpression.Negative(parseMultiply()))
                else -> return left
            }
        }
    }

    fun parseComparisons(): MolangExpression {
        val left = parseAdd()
        return when {
            read("<=") -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.LessEqual)
            read(">=") -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.MoreEqual)
            read('<') -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.Less)
            read('>') -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.More)
            else -> left
        }
    }

    fun parseEquals(): MolangExpression {
        val left = parseComparisons()
        return when {
            read("==") -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.Equal)
            read("!=") -> MolangExpression.Compare(left, parseAdd(), MolangExpression.Compare.Op.NotEqual)
            else -> left
        }
    }

    fun parseAnd(): MolangExpression {
        var left = parseEquals()
        while (true) {
            left = when {
                read("&&") -> MolangExpression.And(left, parseEquals())
                else -> return left
            }
        }
    }

    fun parseOrs(): MolangExpression {
        var left = parseAnd()
        while (true) {
            left = when {
                read("||") -> MolangExpression.Or(left, parseAnd())
                else -> return left
            }
        }
    }

    fun parseTernary(): MolangExpression {
        val condition = parseOrs()
        if (read('?')) {
            val trueCase = parseTernary()
            read(':')
            val falseCase = parseTernary()
            return MolangExpression.Ternary(condition, trueCase, falseCase)
        }

        return condition
    }

    fun parseNullCoalescing(): MolangExpression {
        var left = parseTernary()
        while (read("??")) {
            val right = parseTernary()
            left = MolangExpression.NullCoalescing(left, right)
        }
        return left
    }

    fun parseExpression(): MolangExpression {
        if (read('{')) return parseStatements().also { read('}') }

        return parseNullCoalescing()
    }

    fun parseAssignment(): MolangExpression {
        val left = parseExpression()
        if (!read('=')) return left
        if (left !is MolangExpression.Variable) throw IllegalArgumentException("Cannot assign value to $left")

        val right = parseExpression()
        return MolangExpression.Assignment(left, right)
    }

    fun parseStatement(): MolangExpression {
        return when {
            read("return") -> MolangExpression.Return(parseExpression()).also { returnsCount++ }
            else -> parseAssignment()
        }
    }

    fun parseStatements(): MolangExpression {
        val first = parseStatement()
        if (!read(';')) return first

        val statements = mutableListOf(first)
        while (position < normalizedInput.length && currentChar != '}') {
            statements.add(parseStatement())
            read(';')
        }
        return MolangExpression.Statements(statements)
    }

    fun parseMolang(): MolangExpression {
        var expr = parseStatements()

        if (expr is MolangExpression.Statements && expr.result == MolangExpression.ZERO) {
            val lastExpr = expr.statements.last()
            if (lastExpr is MolangExpression.Return) {
                expr = MolangExpression.Statements(expr.statements.dropLast(1), lastExpr.inner)
                returnsCount--
            }
        }

        if (returnsCount > 0) expr = MolangExpression.ComplexExpr(expr)

        return expr
    }

    fun fullyParseMolang(): MolangExpression {
        val expr = parseMolang()
        if (position < normalizedInput.length) {
            throw IllegalArgumentException("Failed to fully parse input, remaining: ${normalizedInput.substring(position)}")
        }

        return expr
    }

    fun tryFullyParseMolang(): MolangExpression {
        try {
            return fullyParseMolang()
        } catch (e: Exception) {
            throw MolangParserException("Failed to parse molang expression `$normalizedInput`:", e)
        }
    }
}

class MolangParserException(message: String, cause: Throwable?) : Exception(message, cause)

fun String.parseMolangExpression(): MolangExpression = Parser(this).tryFullyParseMolang()
fun JsonPrimitive.parseMolangExpression(): MolangExpression = when {
    isString -> content.parseMolangExpression()
    else -> MolangExpression.Literal(content.toFloat())
}

object MolangSerializer : KSerializer<MolangExpression> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): MolangExpression = parse((decoder as JsonDecoder).decodeJsonElement())
    override fun serialize(encoder: Encoder, value: MolangExpression) =
        throw UnsupportedOperationException("Molang serialization not supported yet!")

    private fun parse(json: JsonElement): MolangExpression = (json as JsonPrimitive).parseMolangExpression()
}

fun main() {
    val e = "q.is_shift".parseMolangExpression()
    println(e)
}