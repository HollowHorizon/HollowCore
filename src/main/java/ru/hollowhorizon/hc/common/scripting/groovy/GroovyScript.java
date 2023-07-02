package ru.hollowhorizon.hc.common.scripting.groovy;

import groovy.lang.GroovySystem;
import kotlin.io.FilesKt;
import net.minecraftforge.common.MinecraftForge;
import org.codehaus.groovy.control.CompilationUnit;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.groovy.sandbox.events.OnScriptingEngineLoadEvent;
import ru.hollowhorizon.hc.common.scripting.groovy.sandbox.events.ScriptingEngineInitEvent;
import ru.hollowhorizon.hc.common.scripting.sandbox.GroovyScriptSandbox;
import ru.hollowhorizon.hc.common.scripting.sandbox.mapper.GroovyDeobfMapper;
import ru.hollowhorizon.hc.common.scripting.sandbox.security.GrSMetaClassCreationHandle;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GroovyScript {
    public static GroovyScriptSandbox sandbox;
    private static final ArrayList<File> SCRIPT_FOLDERS = new ArrayList<>();

    public static void init() {
        HollowCore.LOGGER.info("Loading S.E.");

        sandbox = new GroovyScriptSandbox(SCRIPT_FOLDERS.stream().map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new));

        //MinecraftForge.EVENT_BUS.post(new OnScriptingEngineLoadEvent());
    }

    public static void addSource(File file) {
        SCRIPT_FOLDERS.add(file);
    }

    public static GroovyScriptSandbox getSandbox() {
        return sandbox;
    }

    public static void main(String[] args) {
        GroovyDeobfMapper.init();
        GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(GrSMetaClassCreationHandle.INSTANCE);
        addSource(new File("data"));
        init();
        getSandbox().execute("test.groovy");

        CompilationUnit cu = new CompilationUnit();
        cu.addSource(new File("data/test.groovy"));
        cu.compile();
        cu.getClasses().forEach(f-> {
            FilesKt.writeBytes(new File("data/"+f.getName()+".class"), f.getBytes());
        });
    }
}
