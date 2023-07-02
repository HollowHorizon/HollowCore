package ru.hollowhorizon.hc.mixin.kotlin;

import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaStaticClassScope;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LazyJavaStaticClassScope.class, remap = false)
public interface LazyJavaStaticClassScopeAccessor {

    /**
     * Obtaining a class to search for a mapping later
     */
    @Accessor(value = "jClass")
    JavaClass getJClass();
}
