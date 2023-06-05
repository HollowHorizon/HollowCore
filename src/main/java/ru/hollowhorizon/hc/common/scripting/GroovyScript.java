package ru.hollowhorizon.hc.common.scripting;

import groovy.lang.GroovySystem;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.hollowhorizon.hc.common.scripting.sandbox.GroovySandbox;
import ru.hollowhorizon.hc.common.scripting.sandbox.GroovyScriptSandbox;
import ru.hollowhorizon.hc.common.scripting.sandbox.LoadStage;
import ru.hollowhorizon.hc.common.scripting.sandbox.mapper.GroovyDeobfMapper;
import ru.hollowhorizon.hc.common.scripting.sandbox.security.GrSMetaClassCreationHandle;

import java.net.MalformedURLException;
import java.nio.file.Paths;

public class GroovyScript {
    public static final GroovyScriptSandbox sandbox;

    static {
        try {
            sandbox = new GroovyScriptSandbox(Paths.get("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\run").resolve("hollowscript").toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static GroovyScriptSandbox getSandbox() {
        return sandbox;
    }

    public static void main(String[] args) {
        GroovyDeobfMapper.init();
        GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(GrSMetaClassCreationHandle.INSTANCE);
        getSandbox().run(LoadStage.PRE_INIT);
    }
}
