package ru.hollowhorizon.hc.common.scripting.sandbox;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import net.minecraftforge.fml.loading.FMLPaths;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.sandbox.transformer.GroovyScriptCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GroovyScriptSandbox extends GroovySandbox {

    private static final String[] DEFAULT_IMPORTS = {

    };

    private LoadStage currentLoadStage;
    private boolean checkSyntaxMode = false;

    public GroovyScriptSandbox(URL... scriptEnvironment) {
        super(scriptEnvironment);
        /*
        registerBinding("mods", ModSupport.INSTANCE);
        registerBinding("log", GroovyLog.get());
        registerBinding("EventManager", GroovyEventManager.INSTANCE);
        registerBinding("eventManager", GroovyEventManager.INSTANCE);
        registerBinding("event_manager", GroovyEventManager.INSTANCE);
         */
    }

    public void checkSyntax() {
        load(LoadStage.PRE_INIT, false, true);
        for (LoadStage loadStage : LoadStage.getLoadStages()) {
            if (loadStage != LoadStage.PRE_INIT) {
                load(loadStage, false, false);
            }
        }
    }

    public void checkSyntax(LoadStage loadStage) {
        load(loadStage, false, true);
    }

    public void run(LoadStage currentLoadStage) {
        load(currentLoadStage, true, true);
    }

    public void load(LoadStage currentLoadStage, boolean run, boolean loadClasses) {
        this.checkSyntaxMode = !run;
        this.currentLoadStage = Objects.requireNonNull(currentLoadStage);
        try {
            super.load(run, loadClasses);
        } catch (IOException | ScriptException | ResourceException e) {
            HollowCore.LOGGER.error("An Exception occurred trying to run groovy!", e);
        } catch (Exception e) {
            HollowCore.LOGGER.error(e);
            e.printStackTrace();
        } finally {
            this.currentLoadStage = null;
            this.checkSyntaxMode = false;
        }
    }

    @Override
    public void load(boolean run, boolean loadClasses) throws Exception {
        throw new UnsupportedOperationException("Use run(Loader loader) instead!");
    }

    @Override
    public <T> T runClosure(Closure<T> closure, Object... args) {
        startRunning();
        T result = null;
        try {
            result = closure.call(args);
        } catch (Exception e) {
            HollowCore.LOGGER.error("An exception occurred while running a closure!", e);
        } finally {
            stopRunning();
        }
        return result;
    }

    @Override
    protected void postInitBindings(Binding binding) {
        binding.setVariable("globals", getBindings());
    }

    @Override
    protected void initEngine(GroovyScriptEngine engine, CompilerConfiguration config) {
        config.addCompilationCustomizers(GroovyScriptCompiler.transformer());
        ImportCustomizer importCustomizer = new ImportCustomizer();
        //importCustomizer.addStaticStars(GroovyHelper.class.getName(), MathHelper.class.getName());
        importCustomizer.addImports(DEFAULT_IMPORTS);
        config.addCompilationCustomizers(importCustomizer);
    }

    @Override
    protected void preRun() {
        if (this.checkSyntaxMode) {
            HollowCore.LOGGER.info("Checking syntax in loader '{}'", this.currentLoadStage);
        } else {
            HollowCore.LOGGER.info("Running scripts in loader '{}'", this.currentLoadStage);
        }
        /*
        MinecraftForge.EVENT_BUS.post(new ScriptRunEvent.Pre());
        if (this.currentLoadStage.isReloadable() && !ReloadableRegistryManager.isFirstLoad()) {
            ReloadableRegistryManager.onReload();
            MinecraftForge.EVENT_BUS.post(new GroovyReloadEvent());
        }
        GroovyEventManager.INSTANCE.reset();
         */
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
        /*
        if (this.currentLoadStage == LoadStage.POST_INIT) {
            ReloadableRegistryManager.afterScriptRun();
        }
        MinecraftForge.EVENT_BUS.post(new ScriptRunEvent.Post());
        if (this.currentLoadStage == LoadStage.POST_INIT && ReloadableRegistryManager.isFirstLoad()) {
            ReloadableRegistryManager.setLoaded();
        }
         */
    }

    @Override
    public Collection<File> getClassFiles() {
        //return GroovyScript.getRunConfig().getClassFiles();
        return new ArrayList<>();
    }

    @Override
    public Collection<File> getScriptFiles() {
        //return GroovyScript.getRunConfig().getSortedFiles(this.currentLoadStage.getName());
        ArrayList<File> files = new ArrayList<>();
        files.add(new File("test.groovy"));
        files.add(new File("test2.groovy"));
        return files;
    }

    @Nullable
    public LoadStage getCurrentLoader() {
        return currentLoadStage;
    }
}
