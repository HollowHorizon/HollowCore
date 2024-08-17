package ru.hollowhorizon.hc.client.utils.nbt

import net.minecraft.nbt.Tag

interface INBTSerializable {
    fun serialize(): Tag
    fun deserialize(tag: Tag)
}