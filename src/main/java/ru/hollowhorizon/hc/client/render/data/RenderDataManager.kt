package ru.hollowhorizon.hc.client.render.data

import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel
import ru.hollowhorizon.hc.client.models.core.model.BTModel
import ru.hollowhorizon.hc.client.models.core.model.BakedArmorMeshes
import java.util.*


@OnlyIn(Dist.CLIENT)
class RenderDataManager {
    private val renderData: HashMap<RenderDataKey, IBTRenderDataContainer> = HashMap()

    private inner class RenderDataKey(private val modelName: ResourceLocation, private val combined: Boolean) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as RenderDataKey
            return combined == that.combined && modelName == that.modelName
        }

        override fun hashCode(): Int {
            return Objects.hash(modelName, combined)
        }
    }

    fun tick() {
        for (data in renderData.values) {
            if (data.isInitialized) {
                data.incrementFrameCount()
                if (data.frameSinceLastRender >= FRAMES_BEFORE_CLEANUP) {
                    data.cleanup()
                }
            }
        }
    }

    fun getRenderDataForModel(model: BTModel, doCombined: Boolean): IBTRenderDataContainer {
        val renderKey = RenderDataKey(model.registryName!!, doCombined)
        if (!renderData.containsKey(renderKey)) {
            renderData[renderKey] = BTModelRenderData(model, doCombined)
        }
        return renderData[renderKey]!!
    }

    fun getAnimatedRenderDataForModel(
        model: BTAnimatedModel,
        doCombined: Boolean,
    ): IBTRenderDataContainer? {
        val renderKey = RenderDataKey(model.registryName!!, doCombined)
        if (!renderData.containsKey(renderKey)) {
            renderData[renderKey] = BTAnimatedModelRenderData(model, doCombined)
        }
        return renderData[renderKey]
    }

    fun getArmorRenderDataForModel(armorMeshes: BakedArmorMeshes): BTArmorRenderData {
        val renderKey = RenderDataKey(armorMeshes.name, false)
        if (!renderData.containsKey(renderKey)) {
            renderData[renderKey] = BTArmorRenderData(armorMeshes)
        }
        return renderData[renderKey] as BTArmorRenderData
    }

    companion object {
        val MANAGER = RenderDataManager()
        const val FRAMES_BEFORE_CLEANUP = 60000
    }
}