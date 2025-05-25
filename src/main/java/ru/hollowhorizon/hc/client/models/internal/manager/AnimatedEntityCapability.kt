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

package ru.hollowhorizon.hc.client.models.internal.manager

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.models.internal.Transform
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationType
import ru.hollowhorizon.hc.client.models.internal.controller.AutoController
import ru.hollowhorizon.hc.client.models.internal.controller.Controller
import ru.hollowhorizon.hc.client.models.internal.controller.StateMachineBuilder
import ru.hollowhorizon.hc.client.models.internal.controller.animationController
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapability
import ru.hollowhorizon.hc.common.utils.get
import ru.hollowhorizon.hc.common.utils.molang.MolangCompilerScope
import ru.hollowhorizon.hc.common.utils.rl

/**
 * Represents a data store for an animated object, providing various properties,
 * related to animation layers, textures, and transformations.
 *
 * @property definedLayer Inner layer for automatic animations.
 * @property headLayer Inner layer, especially for head animations.
 * @property rawPose The current pose of the object, can be null.
 * @property model Model, the object with the default value of "%NO_MODEL%" does not have a model.
 * @property layers List of animation layers.
 * @property textures Map of texture identifiers and their corresponding paths.
 * @property animations Map of animation types and their corresponding identifiers.
 * @property transform The transformation applied to the object.
 * @property subModels Map of submodel IDs and their corresponding submodels.
 * @property switchHeadRot Flag indicating whether to toggle the head rotation.
 * @property pose Raw pose of an object, synchronized and can be null.
 */
@HollowCapability(IAnimated::class, Player::class)
class AnimatedEntityCapability : CapabilityInstance() {
    var model by syncable("%NO_MODEL%")
    val textures by syncableMap<String, String>()
    var transform by syncable(Transform())

    var controller by syncable(
        animationController {
            automatic()
            head()
        }
    ) { new, old ->
        var recompile = false
        new.layers.find { it.name == Controller.AUTOMATIC_LAYER }?.let {
            if (model == GLTFEntityRenderer.NO_MODEL) return@let
            val model = GltfManager.getOrCreate(model.rl)
            val stateMachine = AutoController.create(StateMachineBuilder(), AnimationType.load(model.modelTree))
            it.stateMachine = stateMachine.build()
            recompile = true
        }
        new.layers.find { it.name == "__HeadLayer__" }?.let {
            it.stateMachine = StateMachineBuilder().apply {
                state("HeadState") {
                    procedural {
                        onEvaluate {
                            it.setBoneRotation("Head", "q.head_rot")
                        }
                    }
                }

                transition("*", "HeadState") {
                    duration(0.25f)
                }
            }.build()
            recompile = true
        }
        if(recompile) new.recompile()
        new.transferFrom(old)
    }
}

