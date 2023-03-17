package ru.hollowhorizon.hc.common.scripting.annotations

import net.minecraftforge.fml.loading.FMLPaths
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.util.filterByAnnotationType

class HollowScriptConfigurator : RefineScriptCompilationConfigurationHandler {
    private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

    override fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val diagnostics = arrayListOf<ScriptDiagnostic>()

        val annotations =
            context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
                ?: return context.compilationConfiguration.asSuccess()

        val importedSources = linkedMapOf<String, Pair<File, String>>()
        var hasImportErrors = false

        annotations.filterByAnnotationType<Import>().forEach { scriptAnnotation ->
            scriptAnnotation.annotation.paths.forEach { sourceName ->
                val file = sourceName.fromReadablePath()
                val keyPath = file.absolutePath
                val prevImport = importedSources.put(keyPath, file to sourceName)
                if (prevImport != null) {
                    diagnostics.add(
                        ScriptDiagnostic(
                            ScriptDiagnostic.unspecifiedError,
                            "Duplicate imports: \"${prevImport.second}\" and \"$sourceName\"",
                            sourcePath = context.script.locationId,
                            location = scriptAnnotation.location?.locationInText
                        )
                    )
                    hasImportErrors = true
                }
            }
        }

        if (hasImportErrors) return ResultWithDiagnostics.Failure(diagnostics)

        val resolveResult = try {
            @Suppress("DEPRECATION_ERROR")
            internalScriptingRunSuspend {
                resolver.resolveFromScriptSourceAnnotations(annotations.filter { it.annotation is DependsOn || it.annotation is Repository })
            }
        } catch (e: Throwable) {
            diagnostics.add(e.asDiagnostics(path = context.script.locationId))
            ResultWithDiagnostics.Failure(diagnostics)
        }

        return resolveResult.onSuccess { resolvedClassPath ->
            ScriptCompilationConfiguration(context.compilationConfiguration) {
                updateClasspath(resolvedClassPath)
                if (importedSources.isNotEmpty()) importScripts.append(importedSources.values.map { FileScriptSource(it.first) })
            }.asSuccess()
        }
    }
}

fun String.fromReadablePath(): File {
    return FMLPaths.GAMEDIR.get().resolve("hollowscript").resolve(this).toFile()
}