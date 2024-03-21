package ru.hollowhorizon.hc.particles

import com.mojang.logging.LogUtils
import ru.hollowhorizon.hc.particles.client.EffekseerParticlesClient
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import org.slf4j.Logger
import ru.hollowhorizon.hc.HollowCore.MODID

object EffekseerParticles {
    val LOGGER: Logger = LogUtils.getLogger()

    init {

        if (FMLEnvironment.dist.isClient) {
            EffekseerParticlesClient.init()
        }
    }


    fun loc(path: String): ResourceLocation {
        return ResourceLocation(MODID, path)
    }
}