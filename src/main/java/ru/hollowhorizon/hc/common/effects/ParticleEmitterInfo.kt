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

package ru.hollowhorizon.hc.common.effects

import com.mojang.math.Vector4f
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.render.effekseer.ParticleEmitter
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.math.Basis
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.registry.EffectRegistry
import java.util.*

@Serializable
open class ParticleEmitterInfo : Cloneable {
    @JvmField
    val effek: @Serializable(ForResourceLocation::class) ResourceLocation

    @JvmField
    val emitter: @Serializable(ForResourceLocation::class) ResourceLocation?
    private var hasEmitter: Boolean = false
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    open var hasPosition: Boolean = false
    private var rotX: Float = 0f
    private var rotY: Float = 0f
    private var rotZ: Float = 0f
    open var hasRotation: Boolean = false
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    private var scaleZ: Float = 1f
    open var hasScale: Boolean = false
    private var esX: Double = 0.0
    private var esY: Double = 0.0
    private var esZ: Double = 0.0
    open var hasRelativePos: Boolean = false
    private var boundEntity: Int = 0
    open var hasBoundEntity: Boolean = false
    open var isHeadSpace: Boolean = false
    private var target = ""
    private var target2 = ""
    open var hasTarget: Boolean = false
    open var rotateTarget: Boolean = false
    var dynamic0 = 0f
    var dynamic1 = 0f
    var dynamic2 = 0f
    var dynamic3 = 0f

    @JvmOverloads
    constructor(effek: ResourceLocation, emitter: ResourceLocation? = null) {
        this.effek = ResourceLocation(effek.namespace, effek.path.removeSuffix(".efkefc"))
        this.emitter = emitter
        if (emitter != null) hasEmitter = true
    }

    public override fun clone(): ParticleEmitterInfo {
        try {
            return super.clone() as ParticleEmitterInfo
        } catch (e: CloneNotSupportedException) {
            throw IllegalStateException("Failed to clone particle emitter info", e)
        }
    }

    fun position(pos: Vec3): ParticleEmitterInfo {
        return position(pos.x, pos.y, pos.z)
    }

    fun position(x: Double, y: Double, z: Double): ParticleEmitterInfo {
        this.x = x
        this.y = y
        this.z = z
        hasPosition = true
        return this
    }

    fun rotation(rot: Vec2): ParticleEmitterInfo {
        return rotation(rot.x, rot.y, 0f)
    }

    fun rotation(x: Float, y: Float, z: Float): ParticleEmitterInfo {
        this.rotX = x
        this.rotY = y
        this.rotZ = z
        hasRotation = true
        return this
    }

    fun scale(scale: Float): ParticleEmitterInfo {
        return scale(scale, scale, scale)
    }

    fun scale(x: Float, y: Float, z: Float): ParticleEmitterInfo {
        this.scaleX = x
        this.scaleY = y
        this.scaleZ = z
        hasScale = true
        return this
    }

    fun bindOnEntity(entity: Entity): ParticleEmitterInfo {
        this.boundEntity = entity.id
        hasBoundEntity = true
        return this
    }

    fun bindOnTarget(target: String) {
        this.target = target
        hasTarget = true
    }

    fun bindOnTarget2(target: String) {
        this.target2 = target
        hasTarget = true
    }

    fun entitySpaceRelativePosition(pos: Vec3): ParticleEmitterInfo {
        return entitySpaceRelativePosition(pos.x, pos.y, pos.z)
    }

    fun entitySpaceRelativePosition(x: Double, y: Double, z: Double): ParticleEmitterInfo {
        this.esX = x
        this.esY = y
        this.esZ = z
        hasRelativePos = true
        return this
    }

    @JvmOverloads
    fun useEntityHeadSpace(value: Boolean = true): ParticleEmitterInfo {
        this.isHeadSpace = value
        return this
    }

    fun position(): Vec3 {
        return if (hasPosition) Vec3(x, y, z) else Vec3.ZERO
    }

    fun rotation(): Vec3 {
        return if (hasRotation) Vec3(rotX.toDouble(), rotY.toDouble(), rotZ.toDouble()) else Vec3.ZERO
    }

    fun scale(): Vec3 {
        return if (hasScale) Vec3(scaleX.toDouble(), scaleY.toDouble(), scaleZ.toDouble()) else Vec3(1.0, 1.0, 1.0)
    }

    fun getBoundEntity(level: Level): Optional<Entity> {
        return if (hasBoundEntity) Optional.ofNullable(level.getEntity(boundEntity)) else Optional.empty()
    }

    fun spawnInWorld(level: Level, player: Player?) {
        EffectRegistry.get(effek)?.let { effek ->
            val emitter = effek.play(emitterName = this.emitter)
            val x: Float
            val y: Float
            val z: Float
            if (hasPosition) {
                x = this.x.toFloat()
                y = this.y.toFloat()
                z = this.z.toFloat()
            } else if (!hasBoundEntity && player != null) {
                x = player.x.toFloat()
                y = player.y.toFloat()
                z = player.z.toFloat()
            } else {
                z = 0f
                y = z
                x = y
            }
            emitter.setPosition(x, y, z)
            emitter.setDynamicInput(0, dynamic0)
            emitter.setDynamicInput(1, dynamic1)
            emitter.setDynamicInput(2, dynamic2)
            emitter.setDynamicInput(3, dynamic3)

            if (hasRotation) emitter.setRotation(rotX, rotY, rotZ)
            if (hasScale) emitter.setScale(scaleX, scaleY, scaleZ)
            if (hasBoundEntity) {
                val target = level.getEntity(boundEntity)
                val entitySpace = isHeadSpace || hasRelativePos || hasTarget
                emitter.addPreDrawCallback { em: ParticleEmitter, partial: Float ->
                    if (target?.isAlive == true) {
                        val relX: Float
                        val relY: Float
                        val relZ: Float
                        if (entitySpace) {
                            val basis: Basis
                            var rotZ = this.rotZ
                            var rotY: Float
                            var rotX: Float
                            if (isHeadSpace) {
                                rotY = Math.toRadians(target.getViewYRot(partial).toDouble()).toFloat()
                                rotX = Math.toRadians(target.getViewXRot(partial).toDouble()).toFloat()
                                basis = Basis.fromEuler(
                                    Vec3(
                                        rotX.toDouble(),
                                        (Mth.PI - rotY).toDouble(),
                                        rotZ.toDouble()
                                    )
                                )
                            } else {
                                rotY = Math.toRadians(Mth.lerp(partial, target.yRotO, target.yRot).toDouble()).toFloat()
                                rotX = 0f
                                basis = Basis.fromEntityBody(target)
                            }

                            var esX = this.esX
                            var esY = this.esY
                            var esZ = this.esZ

                            if (hasTarget && target is IAnimated) {
                                val capability = target[AnimatedEntityCapability::class]
                                val model = GltfManager.getOrCreate(capability.model.rl)

                                model.findPosition(this.target, target as LivingEntity)?.let {
                                    val pos = Vector4f(0f, 0f, 0f, 1f).apply { transform(it) }

                                    esX += pos.x().toDouble()
                                    esY += pos.y().toDouble()
                                    esZ += pos.z().toDouble()
                                }

                                if (this.target2.isNotEmpty()) {
                                    model.findPosition(this.target2, target as LivingEntity)?.let {
                                        val pos = Vector4f(0f, 0f, 0f, 1f).apply { transform(it) }

                                        dynamic0 = (pos.x() - esX).toFloat()
                                        dynamic1 = (pos.y() - esY).toFloat()
                                        dynamic2 = (pos.z() - esZ).toFloat()
                                    }
                                }

                                if (rotateTarget) {
                                    val rot = model.findRotation(this.target).toXYZ()
                                    rotX += rot.x()
                                    rotY += rot.y()
                                    rotZ += rot.z()
                                }
                            }

                            val esRelPos = basis.toGlobal(Vec3(esX, esY, esZ))

                            relX = (x + esRelPos.x).toFloat()
                            relY = (y + esRelPos.y).toFloat()
                            relZ = (z + esRelPos.z).toFloat()
                            em.setRotation(this.rotX + rotX, this.rotY - rotY, rotZ)
                        } else {
                            relX = x
                            relY = y
                            relZ = z
                        }
                        em.setPosition(
                            (Mth.lerp(partial.toDouble(), target.xOld, target.x).toFloat() + relX),
                            (Mth.lerp(partial.toDouble(), target.yOld, target.y)
                                .toFloat() + relY + (if (isHeadSpace) target.eyeHeight else 0f)),
                            (Mth.lerp(partial.toDouble(), target.zOld, target.z).toFloat() + relZ)
                        )
                    } else em.stop()
                }
            }
        }
    }
}
