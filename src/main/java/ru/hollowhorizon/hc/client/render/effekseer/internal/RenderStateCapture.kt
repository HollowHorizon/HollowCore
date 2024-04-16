/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.render.effekseer.internal

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
