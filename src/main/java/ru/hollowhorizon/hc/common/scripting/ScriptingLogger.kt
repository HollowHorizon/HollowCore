package ru.hollowhorizon.hc.common.scripting

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.appender.FileAppender
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig
import org.apache.logging.log4j.core.layout.PatternLayout
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.scripting.ScriptErrorEvent
import ru.hollowhorizon.hc.common.events.scripting.Severity

object ScriptingLogger {
    val LOGGER = LogManager.getLogger("ScriptingLogger")

    init {
        val context = (LOGGER as org.apache.logging.log4j.core.Logger).context
        val config = context.configuration

        val logPattern = PatternLayout.newBuilder()
            .withPattern("%highlightForge{[%d{HH:mm:ss}] [%t/%level] [%c{2.}/%markerSimpleName]: %minecraftFormatting{%msg{nolookup}}%n%tEx}")
            .build()

        val filePattern = PatternLayout.newBuilder()
            .withPattern("[%d{HH:mm:ss}] [ScriptingLogger/%level]: %msg%n%throwable")
            .build()

        val fileAppender = FileAppender.newBuilder()
            .withFileName("logs/kotlin-scripting.log")
            .withAppend(false)
            .setName("ScriptingLogger")
            .withImmediateFlush(true)
            .setIgnoreExceptions(false)
            .setConfiguration(config)
            .setLayout(filePattern)
            .build()

        val consoleAppender = ConsoleAppender.newBuilder()
            .setName("ConsoleAppender")
            .setLayout(logPattern)
            .build()

        config.addAppender(fileAppender.apply { start() })
        config.addAppender(consoleAppender.apply { start() })

        val loggerConfig = LoggerConfig.createLogger(
            false, Level.INFO, "ScriptingLogger", "true",
            arrayOf(ref("FileAppender"), ref("ConsoleAppender")), null, config, null
        )

        loggerConfig.addAppender(fileAppender, null, null)
        loggerConfig.addAppender(consoleAppender, null, null)
        config.addLogger("ScriptingLogger", loggerConfig)
        context.updateLoggers()
    }

    private fun ref(name: String): AppenderRef {
        return AppenderRef.createAppenderRef(name, null, null)
    }

    @SubscribeEvent
    fun onScriptError(event: ScriptErrorEvent) {
        event.error.forEach {
            when (it.severity) {
                Severity.DEBUG -> {
                    LOGGER.debug(it.format())
                }

                Severity.INFO -> {
                    LOGGER.info(it.format())
                }

                Severity.WARNING -> {
                    LOGGER.warn(it.format())
                }

                Severity.ERROR -> {
                    LOGGER.error(it.format())
                }

                Severity.FATAL -> {
                    LOGGER.fatal(it.format())
                }
            }
        }
    }
}