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

package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.pathfinder.PathType
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.utils.get


class TestEntity(type: EntityType<TestEntity>, world: Level) : PathfinderMob(type, world), IAnimated {

    init {
        this[AnimatedEntityCapability::class].apply {
            model = "${HollowCore.MODID}:models/entity/player_model.gltf"
            //animations[AnimationType.IDLE] = "hello"
            transform = Transform.create {}
        }
        setPathfindingMalus(PathType.WATER, -1.0f)
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(Items.NETHERITE_HOE))
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack(Items.TNT))

        this.setItemSlot(EquipmentSlot.FEET, ItemStack(Items.IRON_BOOTS))
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack(Items.IRON_LEGGINGS))
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack(Items.IRON_CHESTPLATE))
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack(Items.IRON_HELMET))
    }

    override fun registerGoals() {
        super.registerGoals()
        //this.goalSelector.addGoal(0, RandomLookAroundGoal(this))
        //this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0, 10))
    }
}