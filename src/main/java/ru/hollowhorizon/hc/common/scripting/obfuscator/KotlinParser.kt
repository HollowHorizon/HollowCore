package ru.hollowhorizon.hc.common.scripting.obfuscator

import ru.hollowhorizon.hc.common.scripting.obfuscator.general.*
import ru.hollowhorizon.hc.common.scripting.obfuscator.util.createEmptyNode
import java.io.File

object KotlinParser {
    val CLASS_PATTERN = Regex("class\\s+([a-zA-Z0-9_]+)(\\s*:\\s*([a-zA-Z0-9_]+))?\\s*(\\{*})", RegexOption.DOT_MATCHES_ALL)

    @JvmStatic
    fun main(args: Array<String>) {
        val file =
            File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\src\\main\\resources\\assets\\hc\\screen\\test.hs.kts").readText()
                .trim().replace("\r", "")

        val ktFile = parse(file)

        println(ktFile)
    }

    fun parse(code: String, isScript: Boolean = false): KtFile {
        val packageName = code.substringAfter("package ").substringBefore("\n")
        val imports = code.substringAfter("import ").substringBefore("\n").split("\n").map { KtImport(it) }

        return KtFile(packageName, imports, parseClasses(code))

    }

    private fun parseClasses(code: String): List<KtClass> {
        val classes = mutableListOf<KtClass>()

        val classMatches = CLASS_PATTERN.findAll(code).iterator()

        while (classMatches.hasNext()) {
            val classMatch = classMatches.next()
            val className = classMatch.groupValues[1]
            val parentName = classMatch.groupValues[3]
            val classBody = classMatch.groupValues[4]


        }

        return classes
    }

    private fun parseFields(classBody: String): KtField {
        return KtField("test", KtType("Test", null), null)
    }

    private fun parseMethods(classBody: String): KtMethod {
        return KtMethod("test", KtType("Test", null), arrayListOf(), createEmptyNode())
    }

}