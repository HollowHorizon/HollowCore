package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.impl.source.codeStyle.IndentHelper

class HollowIndentHelper: IndentHelper() {
    override fun getIndent(file: PsiFile, node: ASTNode): Int {
        return 0
    }

    override fun getIndent(p0: PsiFile, p1: ASTNode, p2: Boolean): Int {
        return 0
    }
}