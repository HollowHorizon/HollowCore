package ru.hollowhorizon.hc.common.scripting.obfuscator.util

class CodeReader(val code: String) {
    var index = 0

    fun readWord(): String {
        val word = StringBuilder()
        while (index < code.length && code[index].isLetterOrDigit()) {
            word.append(code[index])
            index++
        }
        return word.toString()
    }

    fun navigateToNextWord() {
        while (index < code.length && !code[index].isLetterOrDigit()) {
            index++
        }
    }


}

fun String.reader() = CodeReader(this)