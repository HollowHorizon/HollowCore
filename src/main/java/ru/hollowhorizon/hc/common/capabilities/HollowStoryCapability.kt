package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.nbt.ForCompoundNBT

@HollowCapabilityV2(PlayerEntity::class)
@Serializable
class HollowStoryCapability : IHollowCapability {
    private val storyData: MutableMap<String, @Serializable(ForCompoundNBT::class) CompoundNBT> = HashMap()

    fun addStory(storyName: String, data: CompoundNBT) {
        HollowCore.LOGGER.info("add story")
        storyData[storyName] = data
    }

    fun removeStory(storyName: String) {
        storyData.remove(storyName)
    }

    fun getStory(name: String): CompoundNBT? {
        return storyData[name]
    }

    fun hasStory(storyName: String): Boolean {
        return storyData.containsKey(storyName)
    }

    val all: Set<String>
        get() = storyData.keys
}