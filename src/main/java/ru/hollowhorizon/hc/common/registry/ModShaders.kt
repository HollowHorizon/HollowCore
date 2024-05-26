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

package ru.hollowhorizon.hc.common.registry

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.RegisterShadersEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.render.shaders.RegisterHollowShadersEvent
import ru.hollowhorizon.hc.client.render.shaders.ShaderProgram
import ru.hollowhorizon.hc.client.render.shaders.Uniform
import ru.hollowhorizon.hc.client.utils.rl

object ModShaders {
    lateinit var GLTF_ENTITY: ShaderInstance
    lateinit var GLITCH: ShaderProgram

    @SubscribeEvent
    fun onShaderRegistry(event: RegisterShadersEvent) {
        GLTF_ENTITY = ShaderInstance(
            event.resourceManager,
            ResourceLocation(MODID, "gltf_entity"),
            DefaultVertexFormat.NEW_ENTITY
        )
        event.registerShader(GLTF_ENTITY) {}
    }

    fun onHollowShadersRegistry(event: RegisterHollowShadersEvent) {
        GLITCH = event.create(
            listOf("hc:shaders/program/glitch.hollow.fsh".rl),
            listOf(
                Uniform("DiffuseSampler"),
                Uniform("InSize"),
                Uniform("Time"),
            )
        )
    }
}