package ru.hollowhorizon.hc.mixins

import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class HollowMixinPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String) {
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        return when {
            mixinClassName.contains("iris") -> isLoaded("net.irisshaders.iris.Iris")
            else -> true
        }
    }

    override fun acceptTargets(myTargets: MutableSet<String>?, otherTargets: MutableSet<String>?) {
    }

    override fun getMixins(): MutableList<String>? {
        return null
    }

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?,
    ) {
    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?,
    ) {
    }

    private fun isLoaded(name: String): Boolean {
        try {
            Class.forName(name, false, HollowMixinPlugin::class.java.classLoader)
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
}