package ru.hollowhorizon.hc.client.render.data

import net.minecraft.inventory.EquipmentSlotType
import ru.hollowhorizon.hc.client.models.core.model.BakedArmorMeshes


class BTArmorRenderData(bakedArmor: BakedArmorMeshes) : IBTRenderDataContainer {
    override var frameSinceLastRender = 0
        private set
    override var isInitialized = false
        private set
    private val renderData = HashMap<EquipmentSlotType, BTAnimatedMeshRenderData>().apply {
        put(EquipmentSlotType.HEAD, BTAnimatedMeshRenderData(bakedArmor.head))
        put(EquipmentSlotType.CHEST, BTAnimatedMeshRenderData(bakedArmor.body))
        put(EquipmentSlotType.LEGS, BTAnimatedMeshRenderData(bakedArmor.legs))
        put(EquipmentSlotType.FEET, BTAnimatedMeshRenderData(bakedArmor.feet))
    }

    override fun incrementFrameCount() {
        frameSinceLastRender++
    }

    fun renderSlot(slotType: EquipmentSlotType) {
        if (!this.isInitialized) {
            return
        }
        if (renderData.containsKey(slotType)) {
            renderData[slotType]!!.render()
            frameSinceLastRender = 0
        }
    }

    override fun render() {}
    override fun cleanup() {
        if (!this.isInitialized) {
            return
        }
        for (data in renderData.values) {
            data.cleanup()
        }
        this.isInitialized = false
    }

    override fun upload() {
        for (data in renderData.values) {
            data.upload()
        }
        frameSinceLastRender = 0
        this.isInitialized = true
    }
}