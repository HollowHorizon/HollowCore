package ru.hollowhorizon.loader;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HollowCoreProvider implements IModLanguageProvider {
    private Type MOD_ANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;");


    @Override
    public String name() {
        return "HollowCore Provider";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanData -> {
            //scanData.addLanguageLoader();
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {

    }
}
