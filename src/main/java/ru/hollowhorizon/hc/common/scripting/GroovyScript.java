package ru.hollowhorizon.hc.common.scripting;

import groovy.lang.GroovySystem;
import net.minecraftforge.common.MinecraftForge;
import ru.hollowhorizon.hc.common.scripting.events.ScriptEnvironmentEvent;
import ru.hollowhorizon.hc.common.scripting.sandbox.GroovyScriptSandbox;
import ru.hollowhorizon.hc.common.scripting.sandbox.mapper.GroovyDeobfMapper;
import ru.hollowhorizon.hc.common.scripting.sandbox.security.GrSMetaClassCreationHandle;

import java.net.MalformedURLException;
import java.net.URL;

public class GroovyScript {
    public static final GroovyScriptSandbox sandbox;

    static {
        ScriptEnvironmentEvent event = new ScriptEnvironmentEvent();

        MinecraftForge.EVENT_BUS.post(event);

        sandbox = new GroovyScriptSandbox(event.getScriptFolders().stream().map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new));
    }

    public static GroovyScriptSandbox getSandbox() {
        return sandbox;
    }

    public static void main(String[] args) {
        GroovyDeobfMapper.init();
        GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(GrSMetaClassCreationHandle.INSTANCE);
        getSandbox().execute("test.groovy");
    }
}
