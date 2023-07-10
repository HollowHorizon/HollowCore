package ru.hollowhorizon.hc.common.world.storage

import net.minecraft.nbt.CompoundNBT
import net.minecraft.world.storage.WorldSavedData
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.serialize
import ru.hollowhorizon.hc.common.world.structures.StoryStructureData

class HollowWorldData : WorldSavedData("hollow_world_data") {
    @JvmField
    var STRUCTURE_DATA_LIST = ArrayList<StoryStructureData>()
    override fun load(nbt: CompoundNBT) {
        val structureData = nbt.get("structures") ?: return

        STRUCTURE_DATA_LIST = NBTFormat.deserialize(structureData)
    }
    override fun save(nbt: CompoundNBT): CompoundNBT {
        nbt.put("structures", NBTFormat.serialize(STRUCTURE_DATA_LIST))

        return nbt
    }

    override fun isDirty(): Boolean {
        return true
    }

    companion object {
        @JvmField
        var INSTANCE: HollowWorldData? = null
    }
}
