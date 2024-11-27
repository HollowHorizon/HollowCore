package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.input.PlatformInputJvm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PlatformInputJvm.class, remap = false)
public interface PlatformInputJvmAccessor {
    @Invoker("deriveLocalKeyCodes")
    void deriveKeyCodes();
    @Invoker("createStandardCursors")
    void createCursors();
    @Invoker("installInputHandlers")
    void installInputs(long window);
}
