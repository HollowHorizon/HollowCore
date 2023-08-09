package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.utils.rl

class TestEntityV2(type: EntityType<TestEntityV2>, world: Level) : Mob(type, world) {

    companion object {
        private val IDLE_ANIM = "hc:biped_idle".rl
        private val RUN_ANIM = "hc:biped_run".rl
        private val ZOMBIE_ARMS_ANIM = "hc:biped_zombie_arms".rl
        private val BACKFLIP_ANIM = "hc:biped_backflip".rl
    }

}