package ru.hollowhorizon.hc.particles.client.internal

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Matrix4f
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack

class RenderStateCapture {
    @JvmField
    var hasCapture: Boolean = false
    @JvmField
    val pose: PoseStack = PoseStack()
    @JvmField
    val projection: Matrix4f = Matrix4f()
    @JvmField
    var item: ItemStack? = null
    @JvmField
    var camera: Camera? = null

    companion object {
        @JvmField
        val LEVEL: RenderStateCapture = RenderStateCapture()
        @JvmField
        val CAPTURED_WORLD_DEPTH_BUFFER: RenderTarget = TextureTarget(
            Minecraft.getInstance().window.width,
            Minecraft.getInstance().window.height,
            true, Minecraft.ON_OSX
        )

        @JvmField
        val CAPTURED_HAND_DEPTH_BUFFER: RenderTarget = TextureTarget(
            Minecraft.getInstance().window.width,
            Minecraft.getInstance().window.height,
            true, Minecraft.ON_OSX
        )
    }
}
