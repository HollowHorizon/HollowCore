package ru.hollowhorizon.loader;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

public class HollowCoreTransformationService implements ITransformationService {
    private Logger LOGGER = LogManager.getLogger("HCTransformer");

    public HollowCoreTransformationService() {

        ImagineBreaker.openBootModules();
        ImagineBreaker.wipeFieldFilters();
        ImagineBreaker.wipeMethodFilters();

        System.getProperty("net.bytebuddy.agent.attacher.dump", "true");

        //ByteBuddyAgent.attach(new File("mods/forgefixer-1.0.0.jar"), ProcessHandle.current().pid() + "");
        ByteBuddyAgent.install();

        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (className.equals("java/lang/module/Resolver")) {
                    try {
                        return HCTransformer.transformResolver(classfileBuffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (className.equals("java/lang/ModuleLayer")) {
                    try {
                        return HCTransformer.transformLayer(classfileBuffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return classfileBuffer;
            }
        }, true);

        try {
            // Перезагрузите класс
            instrumentation.retransformClasses(
                    Class.forName("java.lang.module.Resolver"),
                    Class.forName("java.lang.ModuleLayer")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }


        LOGGER.info("HollowCoreTransformationService");
    }


    @Override
    public String name() {
        return "HollowCoreTransformationService";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    public List<ITransformer> transformers() {
        return List.of();
    }
}
