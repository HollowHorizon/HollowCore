package ru.hollowhorizon.hc.common.scripting.remapper

import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtil.offsetToLineNumber
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

internal class PsiPatterns(private val annotationFQN: String) {
    private val patterns = mutableListOf<PsiPattern>()

    fun read(file: PsiFile, replacementFile: String) {
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                method.getAnnotation(annotationFQN) ?: return
                addPattern(file, method, replacementFile)
            }
        })
    }

    private fun addPattern(file: PsiFile, method: PsiMethod, replacementFile: String) {
        val body = method.body!!
        val methodLine = offsetToLineNumber(file.text, body.startOffset)

        val parameters = method.parameterList.parameters.toSet()
        val parameterNames = parameters.map { it.name }.toSet()
        val varArgs = method.parameterList.parameters.lastOrNull()?.isVarArgs ?: false

        val project = file.project
        val psiFileFactory = PsiFileFactory.getInstance(project)
        val replacementPsi = psiFileFactory.createFileFromText(file.language, replacementFile) as PsiJavaFile
        val replacementClass = replacementPsi.classes.first()
        val replacementMethod = replacementClass.findMethodsByName(method.name, false).let { candidates ->
            if (candidates.size > 1) {
                candidates.find { offsetToLineNumber(replacementFile, it.body!!.startOffset) == methodLine }
            } else {
                candidates.firstOrNull()
            } ?: throw RuntimeException("Failed to find updated method \"${method.name}\" (line ${methodLine + 1})")
        }
        val replacementBody = replacementMethod.body!!

        // If the body does not change, then there is no point in applying this pattern
        if (body.text == replacementBody.text) return

        // If either body is empty, then consider the pattern to be disabled
        if (body.statements.isEmpty()) return
        if (replacementBody.statements.isEmpty()) return

        val replacementExpression = when (val statement = replacementBody.statements.last()) {
            is PsiReturnStatement -> statement.returnValue!!
            else -> statement
        }

        val replacement = mutableListOf<String>().also { replacement ->
            val arguments = mutableListOf<PsiExpression>()
            replacementExpression.accept(object : JavaRecursiveElementVisitor() {
                override fun visitReferenceExpression(expr: PsiReferenceExpression) {
                    if (expr.firstChild is PsiReferenceParameterList && expr.referenceName in parameterNames) {
                        arguments.add(expr)
                    } else {
                        super.visitReferenceExpression(expr)
                    }
                }
            })
            val sortedArgs = arguments.toList().sortedBy { it.startOffset }
            var start = replacementExpression.startOffset
            for (argPsi in sortedArgs) {
                replacement.push(replacementFile.slice(start until argPsi.startOffset))
                start = argPsi.endOffset
            }
            replacement.push(replacementFile.slice(start until replacementExpression.endOffset))
        }

        val replacementCanBeAssigned = replacementExpression is PsiReferenceExpression

        patterns.add(PsiPattern(parameters, varArgs, body.statements.last(), replacement, replacementCanBeAssigned))
    }

    fun find(block: PsiCodeBlock): MutableList<PsiPattern.Matcher> {
        val results = mutableListOf<PsiPattern.Matcher>()
        for (pattern in patterns) {
            pattern.find(block.statements, results)
        }
        return results
    }

    fun find(expr: PsiExpression): MutableList<PsiPattern.Matcher> {
        val results = mutableListOf<PsiPattern.Matcher>()
        for (pattern in patterns) {
            pattern.find(expr, results)
        }
        return results
    }
}