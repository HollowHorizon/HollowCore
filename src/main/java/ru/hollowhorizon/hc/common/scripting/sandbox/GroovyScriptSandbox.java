package ru.hollowhorizon.hc.common.scripting.sandbox;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.LoggerLoader;
import ru.hollowhorizon.hc.common.scripting.sandbox.context.IScriptContext;
import ru.hollowhorizon.hc.common.scripting.sandbox.transformer.GroovyScriptCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class GroovyScriptSandbox extends GroovySandbox {
    public static final Logger LOGGER = LoggerLoader.createLogger("ScriptingEngine");

    private static final String[] DEFAULT_IMPORTS = {

    };
    private boolean checkSyntaxMode = false;

    public GroovyScriptSandbox(URL... scriptEnvironment) {
        super(scriptEnvironment);

        registerBinding("log", HollowCore.LOGGER);
    }

    public void applyContext(IScriptContext context) {
        this.context = context;
    }

    public void checkSyntax(Collection<File> scripts, Collection<File> libraries) {
        load(scripts, libraries, false, true);
    }

    public void executeScripts(Collection<File> scripts, Collection<File> libraries) {
        load(scripts, libraries, true, true);
    }

    public void execute(Collection<String> scripts, Collection<String> libraries) {
        executeScripts(scripts.stream().map(File::new).collect(Collectors.toList()), libraries.stream().map(File::new).collect(Collectors.toList()));
    }

    public void execute(String script) {
        execute(Collections.singleton(script), Collections.emptyList());
    }

    protected void load(Collection<File> scripts, Collection<File> libraries, boolean run, boolean loadClasses) {
        this.checkSyntaxMode = !run;
        try {
            super.execute(scripts, libraries, run, loadClasses);
        } catch (IOException | ScriptException | ResourceException e) {
            if (this.context != null) this.context.onError("An Exception occurred trying to run groovy!", e);
        } catch (Exception e) {
            if (this.context != null) this.context.onError(e);
        } finally {
            this.checkSyntaxMode = false;
        }
    }

    @Override
    public void execute(Collection<File> scripts, Collection<File> libraries, boolean run, boolean loadClasses) throws Exception {
        throw new UnsupportedOperationException("Use run(Loader loader) instead!");
    }

    @Override
    public <T> T runClosure(Closure<T> closure, Object... args) {
        startRunning();
        T result = null;
        try {
            result = closure.call(args);
        } catch (Exception e) {
            if (this.context != null) this.context.onError("An exception occurred while running a closure!", e);
        } finally {
            stopRunning();
        }
        return result;
    }

    @Override
    protected void initEngine(GroovyScriptEngine engine, CompilerConfiguration config) {
        config.addCompilationCustomizers(GroovyScriptCompiler.transformer());
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addImports(DEFAULT_IMPORTS);
        if (this.context != null) config.setScriptBaseClass(this.context.getBaseClass().getName());
        config.addCompilationCustomizers(importCustomizer);
    }

    @Override
    protected void postInitBindings(Binding binding) {
        if (this.context != null) this.context.getBindings().forEach(binding::setProperty);
    }

    @Override
    protected void preRun() {
        if (this.checkSyntaxMode) {
            HollowCore.LOGGER.info("Checking scripts syntax.");
        } else {
            HollowCore.LOGGER.info("Running scripts.");
        }
    }

    @Override
    protected boolean shouldRunFile(File file) {
        if (!this.checkSyntaxMode) {
            HollowCore.LOGGER.info(" - executing {}", file.toString());
        }
        return true;
    }

    @Override
    protected void postRun() {
    }
}
