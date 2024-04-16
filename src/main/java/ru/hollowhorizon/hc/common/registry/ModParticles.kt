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

import net.minecraft.client.particle.SpriteSet
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType

object ModParticles : HollowRegistry() {
    private val CIRCLE by register("circle", ::HollowParticleType)
    private val STAR by register("star", ::HollowParticleType)

    private val GENERATED_LIST = ArrayList<RegistryObject<HollowParticleType>>()

    @JvmStatic
    fun onRegisterParticles(event: RegisterParticleProvidersEvent) {
        event.register(CIRCLE.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        event.register(STAR.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        GENERATED_LIST.forEach {
            event.register(it.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        }
    }

    fun addParticle(name: String) {
        val particle by register(name, ::HollowParticleType)
        GENERATED_LIST.add(particle)
    }
}