package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.models.gltf.manager.*
import ru.hollowhorizon.hc.client.utils.get


class TestEntity(type: EntityType<TestEntity>, world: Level) : PathfinderMob(type, world), IAnimated {

    init {
        this[AnimatedEntityCapability::class].apply {
            model = "hc:models/entity/player_model.gltf"
            subModels["Head"] = SubModel(
                "hc:models/entity/player_model.gltf",
                arrayListOf(AnimationLayer("sit_anim", LayerMode.ADD, PlayMode.LOOPED, 1f)),
                HashMap(),
                Transform(sX = 0.5f, sY = 0.5f, sZ = 0.5f, tY = 0.5f),
                HashMap()
            )
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(Items.NETHERITE_HOE))
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack(Items.TNT))

        this.setItemSlot(EquipmentSlot.FEET, ItemStack(Items.IRON_BOOTS))
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack(Items.IRON_LEGGINGS))
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack(Items.IRON_CHESTPLATE))
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack(Items.IRON_HELMET))
    }

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(0, RandomLookAroundGoal(this))
        this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0, 10))
    }
}