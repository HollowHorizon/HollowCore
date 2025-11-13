package ru.hollowhorizon.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class HollowCoreAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("Can redefine: " + instrumentation.isRedefineClassesSupported());
        System.out.println("Can retransform: " + instrumentation.isRetransformClassesSupported());

        try {
            instrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                    if (className.equals("java/lang/module/Resolver")) {
                        try {
                            var bytecode = HCTransformer.transformResolver(classfileBuffer);
                            new FileOutputStream("Resolver.class").write(bytecode);
                            return bytecode;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (className.equals("java/lang/ModuleLayer")) {
                        try {
                            var bytecode = HCTransformer.transformLayer(classfileBuffer);

                            new FileOutputStream("ModuleLayer.class").write(bytecode);

                            return bytecode;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return classfileBuffer;
                }
            }, true);
            System.out.println("HollowCoreAgent: Class patched successfully!");
        } catch (Exception e) {
            System.err.println("HollowCoreAgent: premain exception: " + e);
            e.printStackTrace();
        }

        try {
            instrumentation.retransformClasses(
                    Class.forName("java.lang.module.Resolver"),
                    Class.forName("java.lang.ModuleLayer")
            );
            System.out.println("HollowCoreAgent: Class reloaded successfully!");
        } catch (Exception e) {
            System.out.println("HollowCoreAgent: Class reload failed!");
            e.printStackTrace();
        }


        System.out.println("HollowCoreTransformationService");
    }
}
