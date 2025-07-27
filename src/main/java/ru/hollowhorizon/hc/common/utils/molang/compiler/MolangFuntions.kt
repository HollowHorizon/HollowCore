package ru.hollowhorizon.hc.common.utils.molang.compiler

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import ru.hollowhorizon.hc.common.utils.molang.parser.AstFloat

object MolangFuntions {
    private data class Signature(
        val argCount: Int,
        val builder: (List<AstFloat>) -> AstFloat
    )

    private val functions = Object2ObjectOpenHashMap<String, Signature>()

    fun addFunction(
        name: String,
        argCount: Int,
        builder: (List<AstFloat>) -> AstFloat
    ) {
        functions[name] = Signature(argCount, builder)
    }

    fun resolve(name: String, args: List<AstFloat>): AstFloat {
        val sig = functions[name] ?: error("Function '$name' is not registered")
        require(sig.argCount == args.size) { "Function '$name' expects ${sig.argCount} args, got ${args.size}" }
        return sig.builder(args)
    }
}