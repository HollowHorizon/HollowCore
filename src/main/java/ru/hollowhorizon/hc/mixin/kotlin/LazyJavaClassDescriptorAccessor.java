package ru.hollowhorizon.hc.mixin.kotlin;

import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaClassDescriptor;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LazyJavaClassDescriptor.class, remap = false)
public interface LazyJavaClassDescriptorAccessor {
    @Accessor(value = "jClass")
    JavaClass getJClass();
}
