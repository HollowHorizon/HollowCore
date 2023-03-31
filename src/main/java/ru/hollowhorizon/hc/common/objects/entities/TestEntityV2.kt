package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.world.World
import ru.hollowhorizon.hc.client.utils.rl

class TestEntityV2(type: EntityType<TestEntityV2>, world: World) : MobEntity(type, world) {

    companion object {
        private val IDLE_ANIM = "hc:biped_idle".rl
        private val RUN_ANIM = "hc:biped_run".rl
        private val ZOMBIE_ARMS_ANIM = "hc:biped_zombie_arms".rl
        private val BACKFLIP_ANIM = "hc:biped_backflip".rl
    }

}