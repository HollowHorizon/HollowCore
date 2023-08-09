package scripts

import com.github.jengelman.gradle.plugins.shadow.relocation.RelocateClassContext
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocatePathContext
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.codehaus.plexus.util.SelectorUtils
class HollowRelocator : Relocator {
    @get:Input
    val patternIn: String

    @get:Input
    val pathPatternIn: String

    @get:Input
    val shadedPatternIn: String

    @get:Input
    val shadedPathPatternIn: String

    @get:Input
    val includesIn: HashSet<String>

    @get:Input
    val excludesIn: HashSet<String>

    @get:Input
    val rawStringIn: Boolean

    constructor(patt: String, shadedPattern: String, includes: List<String>, excludes: List<String>) : this(
        patt,
        shadedPattern,
        includes,
        excludes,
        false
    )

    constructor(
        patt: String,
        shadedPattern: String,
        includes: List<String>,
        excludes: List<String>,
        rawString: Boolean
    ) {
        this.rawStringIn = rawString

        if (rawString) {
            this.pathPatternIn = patt
            this.shadedPathPatternIn = shadedPattern

            this.patternIn = patt // fix null error
            this.shadedPatternIn = shadedPattern // fix null error
        } else {
            this.patternIn = patt.replace('/', '.')
            this.pathPatternIn = patt.replace('.', '/')

            this.shadedPatternIn = shadedPattern.replace('/', '.')
            this.shadedPathPatternIn = shadedPattern.replace('.', '/')
        }

        this.includesIn = normalizePatterns(*includes.toTypedArray())
        this.excludesIn = normalizePatterns(*excludes.toTypedArray())
    }

    fun include(pattern: String): HollowRelocator {
        this.includesIn.addAll(normalizePatterns(pattern))
        return this
    }

    fun exclude(pattern: String): HollowRelocator {
        this.excludesIn.addAll(normalizePatterns(pattern))
        return this
    }

    private fun normalizePatterns(vararg patterns: String): HashSet<String> {
        val normalized = LinkedHashSet<String>()

        if (patterns.isNotEmpty()) {

            for (pattern in patterns) {
                // Regex patterns don't need to be normalized and stay as is
                if (pattern.startsWith(SelectorUtils.REGEX_HANDLER_PREFIX)) {
                    normalized.add(pattern)
                    continue
                }

                val classPattern = pattern.replace('.', '/')

                normalized.add(classPattern)

                if (classPattern.endsWith("/*")) {
                    val packagePattern = classPattern.substring(0, classPattern.lastIndexOf('/'))
                    normalized.add(packagePattern)
                }
            }
        }

        return normalized
    }

    private fun isIncluded(path: String): Boolean {
        if (includesIn.isNotEmpty()) {
            for (include in includesIn) {
                if (SelectorUtils.matchPath(include, path, "/", true)) {
                    return true
                }
            }
            return false
        }
        return true
    }

    private fun isExcluded(path: String): Boolean {
        if (excludesIn.isNotEmpty()) {
            for (exclude in excludesIn) {
                if (SelectorUtils.matchPath(exclude, path, "/", true)) {
                    return true
                }
            }
        }
        return false
    }

    override fun canRelocatePath(path: String): Boolean {
        var pathMutable = path
        if (rawStringIn) {
            return Pattern.compile(pathPatternIn).matcher(pathMutable).find()
        }

        // If string is too short - no need to perform expensive string operations
        if (pathMutable.length < pathPatternIn.length) {
            return false
        }

        if (pathMutable.endsWith(".class")) {
            // Safeguard against strings containing only ".class"
            if (pathMutable.length == 6) {
                return false
            }
            pathMutable = pathMutable.substring(0, pathMutable.length - 6)
        }

        // Allow for annoying option of an extra / on the front of a path. See MSHADE-119 comes from getClass().getResource("/a/b/c.properties").
        val pathStartsWithPattern =
            if (pathMutable.first() == '/') path.startsWith(pathPatternIn, 1) else path.startsWith(pathPatternIn)
        if (pathStartsWithPattern) {
            return isIncluded(pathMutable) && !isExcluded(pathMutable)
        }
        return false
    }

    override fun canRelocateClass(className: String): Boolean {
        return !rawStringIn &&
                className.indexOf('/') < 0 &&
                canRelocatePath(className.replace('.', '/'))
    }

    override fun relocatePath(context: RelocatePathContext): String {
        val path = context.path
        context.stats.relocate(pathPatternIn, shadedPathPatternIn)
        return if (rawStringIn) {
            path.replace(pathPatternIn, shadedPathPatternIn)
        } else {
            path.replaceFirst(pathPatternIn, shadedPathPatternIn)
        }
    }

    override fun relocateClass(context: RelocateClassContext): String {
        val clazz = context.className
        context.stats.relocate(pathPatternIn, shadedPathPatternIn)
        return clazz.replaceFirst(patternIn, shadedPatternIn)
    }

    override fun applyToSourceContent(sourceContent: String): String {
        return if (rawStringIn) {
            sourceContent
        } else {
            sourceContent.replace("\\b$patternIn", shadedPatternIn)
        }
    }
}