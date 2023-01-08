package ru.hollowhorizon.hc.common.scripting.remapper

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

internal class AutoImports(private val environment: KotlinCoreEnvironment) {

    private val shortClassNames = ShortNameIndex(environment)

    fun apply(originalFile: PsiFile, mappedFile: String, processedFile: String): String =
        apply(originalFile, originalFile.text.lines(), mappedFile.lines(), processedFile.lines())

    private fun apply(
        originalFile: PsiFile,
        originalLines: List<String>,
        mappedLines: List<String>,
        processedLines: List<String>,
    ): String {
        if (originalLines.size != mappedLines.size || originalLines.size != processedLines.size) {
            return mappedLines.joinToString("\n")
        }

        val inputLines = processedLines.mapIndexed { index, processedLine ->
            if (originalLines[index] == processedLine) {
                mappedLines[index]
            } else {
                processedLine
            }
        }
        val inputText = inputLines.joinToString("\n")

        val psiFileFactory = PsiFileFactory.getInstance(environment.project)
        val psiFile =
            psiFileFactory.createFileFromText(originalFile.language, inputText) as? PsiJavaFile ?: return inputText
        val pkg = psiFile.packageStatement?.packageReference?.resolve() as? PsiPackage

        val references = findOutgoingReferences(psiFile)

        val imports = psiFile.importList?.importStatements ?: emptyArray()
        val onDemandImports = imports.filter { it.isOnDemand }.mapNotNull { it.qualifiedName }.map { "$it." }.toSet()
        val existingImports = imports.filter { !it.isOnDemand }.mapNotNull { it.qualifiedName }.toSet()
        val unusedImports = existingImports.filter { it.substringAfterLast(".") !in references }.toSet()

        val implicitReferenceSources = listOfNotNull(
            psiFile.classes.flatMap { it.allInnerClasses.asIterable() },
            pkg?.classes?.asIterable(),
        )
        val implicitReferences = implicitReferenceSources.flatten().mapNotNull { it.name }.toSet()
        val importedReferences = existingImports.map { it.substringAfterLast(".") }.toSet()
        val missingReferences = references.asSequence() - importedReferences - implicitReferences
        val newImports = missingReferences.mapNotNull { shortClassNames[it].singleOrNull()?.qualifiedName }
            .filter { ref -> onDemandImports.none { ref.startsWith(it) } }
            .filter { !it.startsWith("java.lang.") }

        val finalImports = existingImports.toSet() - unusedImports.toSet() + newImports + onDemandImports.map { "$it*" }

        val textBuilder = StringBuilder(inputText)

        imports.map { it.textRange }.sortedByDescending { it.startOffset }.forEach { importRange ->
            textBuilder.replace(importRange.startOffset, importRange.endOffset, "")

            val start = importRange.startOffset
            val whiteSpaceRange = start - 1..start
            if (whiteSpaceRange.first in textBuilder.indices && whiteSpaceRange.last in textBuilder.indices) {
                val whiteSpaceReplacement = when (textBuilder.substring(whiteSpaceRange)) {
                    "\n\n" -> "\n"
                    "\n " -> "\n"
                    " \n" -> "\n"
                    "  " -> " "
                    else -> null
                }
                if (whiteSpaceReplacement != null) {
                    textBuilder.replace(whiteSpaceRange.first, whiteSpaceRange.last + 1, whiteSpaceReplacement)
                }
            }
        }

        val startOfImports = psiFile.importList?.takeIf { it.textLength > 0 }?.startOffset
        val endOfPackage = psiFile.packageStatement?.endOffset ?: 0

        val removedLineCount = inputLines.size - textBuilder.lineSequence().count()
        textBuilder.insert(startOfImports ?: endOfPackage, "\n".repeat(removedLineCount))

        var index = startOfImports ?: endOfPackage

        if (startOfImports == null) {
            repeat(2) {
                if (textBuilder[index + 1] == '\n' && textBuilder[index + 2] == '\n') {
                    index++
                }
            }
        }

        val javaImports = finalImports.filter { it.startsWith("java.") || it.startsWith("javax.") }.toSet()
        val otherImports = finalImports - javaImports
        val importGroups = listOf(otherImports, javaImports).filter { it.isNotEmpty() }

        for ((importGroupIndex, importGroup) in importGroups.withIndex()) {
            val hasMoreGroups = importGroupIndex + 1 in importGroups.indices

            for (import in importGroup.sorted()) {
                val hasPrecedingStatement = index > 0 && textBuilder[index - 1] != '\n'
                val canAdvanceToNextLine = textBuilder[index + 1] == '\n' && textBuilder[index + 2] == '\n'

                val str = (if (hasPrecedingStatement) " " else "") + "import $import;"
                textBuilder.insert(index, str)
                index += str.length + if (canAdvanceToNextLine) 1 else 0
            }

            if (hasMoreGroups && textBuilder[index + 1] == '\n' && textBuilder[index + 2] == '\n') {
                index++
            }
        }

        return textBuilder.toString()
    }

    private fun findOutgoingReferences(file: PsiJavaFile): Set<String> {
        val references = mutableSetOf<String>()

        fun recordReference(reference: PsiJavaCodeReferenceElement) {
            if (reference.isQualified) return
            val name = reference.referenceName ?: return
            if (!name.first().isUpperCase()) return
            val resolved = reference.resolve()
            if (resolved is PsiTypeParameter) return
            if (resolved is PsiVariable) return
            references.add(name)
        }

        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                recordReference(reference)
                super.visitReferenceElement(reference)
            }
        })
        return references
    }
}