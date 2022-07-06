package ru.hollowhorizon.hc.common.oldmods;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import ru.hollowhorizon.hc.HollowCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.LOADING;

public class OldModLanguageProvider implements IModLanguageProvider {
    private static final Type MODANNOTATION = Type.getType("Lcpw.mods.fml.common.Mod;");

    @Override
    public String name() {
        return "hollowforge";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {

        return (scanData) -> scanData.addLanguageLoader(
                scanData.getAnnotations().stream()
                        .filter(data -> data.getAnnotationType().toString().equals("Lcpw/mods/fml/common/Mod;") || data.getAnnotationType().toString().equals("Lnet/minecraftforge/fml/common/Mod;"))
                        .map(ad -> new HollowModTarget(ad.getClassType().getClassName(), ((String) ad.getAnnotationData().get("modid")).toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toMap(HollowModTarget::getModId, Function.identity(), (a, b) -> a))
        );
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {

    }

    private static class HollowModTarget implements IModLanguageProvider.IModLanguageLoader {
        private final String className;
        private final String modId;

        private HollowModTarget(String className, String modId) {
            HollowCore.LOGGER.info("creating constructor for mod: " + modId);
            this.className = className;
            this.modId = modId;
        }

        public String getModId() {
            return modId;
        }

        @Override
        public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
            try {
                final Class<?> fmlContainer = Class.forName("net.minecraftforge.fml.javafmlmod.FMLModContainer", true, Thread.currentThread().getContextClassLoader());
                HollowCore.LOGGER.debug(LOADING, "Loading FMLModContainer from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), fmlContainer.getClassLoader());
                final Constructor<?> constructor = fmlContainer.getConstructor(IModInfo.class, String.class, ClassLoader.class, ModFileScanData.class);
                return (T) constructor.newInstance(info, className, modClassLoader, modFileScanResults);
            } catch (InvocationTargetException e) {
                HollowCore.LOGGER.fatal(LOADING, "Failed to build mod", e);
                final Class<RuntimeException> mle = (Class<RuntimeException>) LamdbaExceptionUtils.uncheck(() -> Class.forName("net.minecraftforge.fml.ModLoadingException", true, Thread.currentThread().getContextClassLoader()));
                if (mle.isInstance(e.getTargetException())) {
                    throw mle.cast(e.getTargetException());
                } else {
                    final Class<ModLoadingStage> mls = (Class<ModLoadingStage>) LamdbaExceptionUtils.uncheck(() -> Class.forName("net.minecraftforge.fml.ModLoadingStage", true, Thread.currentThread().getContextClassLoader()));
                    throw LamdbaExceptionUtils.uncheck(() -> LamdbaExceptionUtils.uncheck(() -> mle.getConstructor(IModInfo.class, mls, String.class, Throwable.class)).newInstance(info, Enum.valueOf(mls, "CONSTRUCT"), "fml.modloading.failedtoloadmodclass", e));
                }
            } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                HollowCore.LOGGER.fatal(LOADING, "Unable to load FMLModContainer, wut?", e);
                final Class<RuntimeException> mle = (Class<RuntimeException>) LamdbaExceptionUtils.uncheck(() -> Class.forName("net.minecraftforge.fml.ModLoadingException", true, Thread.currentThread().getContextClassLoader()));
                final Class<ModLoadingStage> mls = (Class<ModLoadingStage>) LamdbaExceptionUtils.uncheck(() -> Class.forName("net.minecraftforge.fml.ModLoadingStage", true, Thread.currentThread().getContextClassLoader()));
                throw LamdbaExceptionUtils.uncheck(() -> LamdbaExceptionUtils.uncheck(() -> mle.getConstructor(IModInfo.class, mls, String.class, Throwable.class)).newInstance(info, Enum.valueOf(mls, "CONSTRUCT"), "fml.modloading.failedtoloadmodclass", e));
            }
        }
    }
}
