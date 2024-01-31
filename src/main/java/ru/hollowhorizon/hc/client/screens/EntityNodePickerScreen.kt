package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import ru.hollowhorizon.hc.client.models.gltf.ModelData
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.rl

class EntityNodePickerScreen : HollowScreen() {
    val model = GltfManager.getOrCreate("hc:models/entity/player_model.gltf".rl)

    var clicked = false

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick)

        pPoseStack.pushPose()
        pPoseStack.translate(width / 2.0, height / 2.0, 100.0)
        pPoseStack.scale(100f, 100f, -100f)

        model.render(pPoseStack, ModelData(null, null, null, null), {
            Minecraft.getInstance().textureManager.getTexture(it).id
        }, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY)
        if (clicked) model.pickColor(pPoseStack, pMouseX.toDouble(), pMouseY.toDouble())
        pPoseStack.popPose()
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        clicked = true
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun mouseReleased(p_231048_1_: Double, p_231048_3_: Double, p_231048_5_: Int): Boolean {
        clicked = false
        return super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_)
    }
}