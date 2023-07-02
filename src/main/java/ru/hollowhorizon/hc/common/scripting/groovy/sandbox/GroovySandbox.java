package ru.hollowhorizon.hc.common.scripting.sandbox;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fml.loading.FMLPaths;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.sandbox.context.IScriptContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author brachy84
 */
public abstract class GroovySandbox {

    private static final ThreadLocal<GroovySandbox> currentSandbox = new ThreadLocal<>();
    // TODO
    private String currentScript = null;
    private int currentLine = -1;
    protected IScriptContext context;

    @Nullable
    public static GroovySandbox getCurrentSandbox() {
        return currentSandbox.get();
    }

    private final URL[] scriptEnvironment;
    private final ThreadLocal<Boolean> running = ThreadLocal.withInitial(() -> false);
    private final Map<String, Object> bindings = new Object2ObjectOpenHashMap<>();

    protected GroovySandbox(URL[] scriptEnvironment) {
        if (scriptEnvironment == null || scriptEnvironment.length == 0) {
            throw new NullPointerException("Script Environment must be non null and at least contain one URL!");
        }
        this.scriptEnvironment = scriptEnvironment;
    }

    protected GroovySandbox(List<URL> scriptEnvironment) {
        this(scriptEnvironment.toArray(new URL[0]));
    }

    public void registerBinding(String name, Object obj) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(obj);

        bindings.put(name, obj);
    }

    protected void startRunning() {
        currentSandbox.set(this);
        this.running.set(true);
    }

    protected void stopRunning() {
        this.running.set(false);
        currentSandbox.set(null);
    }

    protected void execute(Collection<File> scripts, Collection<File> libraries, boolean run, boolean loadClasses) throws Exception {
        currentSandbox.set(this);
        preRun();

        GroovyScriptEngine engine = new GroovyScriptEngine(this.scriptEnvironment, Thread.currentThread().getContextClassLoader());
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        engine.setConfig(config);
        initEngine(engine, config);
        Binding binding = new Binding(bindings);
        postInitBindings(binding);
        Set<File> executedClasses = new ObjectOpenHashSet<>();

        running.set(run);
        try {
            if (loadClasses) {
                // load and run any configured class files
                loadClassScripts(engine, libraries, binding, executedClasses, run);
            }
            // now run all script files
            loadScripts(engine, scripts, binding, executedClasses, run);
        } finally {
            running.set(false);
            postRun();
            currentSandbox.set(null);
            setCurrentScript(null);
        }
    }

    protected void loadScripts(GroovyScriptEngine engine, Collection<File> scripts, Binding binding, Set<File> executedClasses, boolean run) {
        for (File scriptFile : scripts) {
            if (!executedClasses.contains(scriptFile)) {
                Class<?> clazz = loadScriptClass(engine, scriptFile, true);
                if (clazz == null) {
                    if (this.context != null) {
                        this.context.onError("Error loading script for " + scriptFile.getPath());
                        this.context.onError("Did you forget to register your class file in your run config?");
                    }
                    continue;
                }
                if (shouldRunFile(scriptFile)) {
                    Script script = InvokerHelper.createScript(clazz, binding);

                    if(this.context!=null) this.context.onRunScriptPre(script);

                    if (run) {
                        setCurrentScript(scriptFile.toString());
                        script.run();
                        setCurrentScript(null);
                    }

                    if(this.context!=null) this.context.onRunScriptPost(script);
                }
            }
        }
    }

    protected void loadClassScripts(GroovyScriptEngine engine, Collection<File> scripts, Binding binding, Set<File> executedClasses, boolean run) {
        for (File classFile : scripts) {
            Class<?> clazz = loadScriptClass(engine, classFile, false);
            if (clazz == null) {
                // loading script fails if the file is a script that depends on a class file that isn't loaded yet
                // we cant determine if the file is a script or a class
                continue;
            }
            // the superclass of class files is Object
            if (clazz.getSuperclass() != Script.class && shouldRunFile(classFile)) {
                executedClasses.add(classFile);
                Script script = InvokerHelper.createScript(clazz, binding);
                if (run) {
                    setCurrentScript(script.toString());
                    script.run();
                    setCurrentScript(null);
                }
            }
        }
    }

    public <T> T runClosure(Closure<T> closure, Object... args) {
        startRunning();
        T result = null;
        try {
            result = closure.call(args);
        } catch (Exception e) {
            if (this.context != null) this.context.onError("Caught an exception trying to run a closure:", e);
        } finally {
            stopRunning();
        }
        return result;
    }

    protected void postInitBindings(Binding binding) {

    }

    protected void initEngine(GroovyScriptEngine engine, CompilerConfiguration config) {
    }

    protected void preRun() {
    }

    protected boolean shouldRunFile(File file) {
        return true;
    }

    protected void postRun() {
    }


    public boolean isRunning() {
        return this.running.get();
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    protected void setCurrentScript(String currentScript) {
        this.currentScript = currentScript;
        this.currentLine = -1;
    }

    public static String getRelativePath(String source) {
        try {
            Path path = Paths.get(new URL(source).toURI());
            Path mainPath = FMLPaths.GAMEDIR.get().resolve("hollowscript");
            return mainPath.relativize(path).toString();
        } catch (URISyntaxException | MalformedURLException e) {
            HollowCore.LOGGER.error("Error parsing script source '{}'", source);
            // don't log to GroovyLog here since it will cause a StackOverflow
            return source;
        }
    }

    private Class<?> loadScriptClass(GroovyScriptEngine engine, File file, boolean printError) {
        Class<?> scriptClass = null;
        try {
            try {
                // this will only work for files that existed when the game launches
                scriptClass = engine.loadScriptByName(file.toString());
                // extra safety
                if (scriptClass == null) {
                    scriptClass = tryLoadDynamicFile(engine, file);
                }
            } catch (ResourceException e) {
                // file was added later, causing a ResourceException
                // try to manually load the file
                scriptClass = tryLoadDynamicFile(engine, file);
            }

            // if the file is still not found something went wrong
        } catch (Exception e) {
            if (printError) {
                if (this.context != null) this.context.onError(e);
            }
        }

        System.out.println("Loading class! "+ scriptClass);
        writeClass(engine, scriptClass);

        return scriptClass;
    }

    private void writeClass(GroovyScriptEngine engine, Class<?> scriptClass) {

        File file = new File("cache.class");



    }

    @Nullable
    private Class<?> tryLoadDynamicFile(GroovyScriptEngine engine, File file) throws ResourceException {
        Path path = null;
        for (URL root : this.scriptEnvironment) {
            try {
                File rootFile = new File(root.toURI());
                // try to combine the root with the file ending
                path = new File(rootFile, file.toString()).toPath();
                if (Files.exists(path)) {
                    // found a valid file
                    break;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (path == null) return null;

        HollowCore.LOGGER.info("Found path '{}' for dynamic file {}", path, file.toString());

        Class<?> clazz = null;
        try {
            // manually load the file as a groovy script
            clazz = engine.getGroovyClassLoader().parseClass(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
