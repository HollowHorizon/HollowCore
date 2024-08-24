package ru.hollowhorizon.classpathfixer;

import net.minecraftforge.bootstrap.api.BootstrapClasspathModifier;
import sun.misc.Unsafe;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class HollowCoreClasspathModifier implements BootstrapClasspathModifier {

    @Override
    public String name() {
        return "ModuleRejector";
    }

    @Override
    public boolean process(List<Path[]> classpath) {
        try {
            var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            var defined = Class.forName("jdk.internal.module.Checks").getDeclaredField("RESERVED");
            // Если Kotlin можно называть свои пакеты `native`, то почему мне нельзя?
            unsafe.putObject(unsafe.staticFieldBase(defined), unsafe.staticFieldOffset(defined), Set.of());

            var blacklist = Arrays.asList(
                    "annotations-23.0.0.jar", "jcip-annotations-1.0.jar", "jsr305-3.0.2.jar", // Лишние аннотации
                    "kotlinforforge-5.5.0-all.jar" // У меня уже есть Kotlin, давайте выкинем этот.
            );

            return classpath.removeIf(path ->
                    blacklist.stream().anyMatch(it -> path[0].toAbsolutePath().toString().endsWith(it))
            );
        } catch (Exception e) {
            return false;
        }
    }
}
