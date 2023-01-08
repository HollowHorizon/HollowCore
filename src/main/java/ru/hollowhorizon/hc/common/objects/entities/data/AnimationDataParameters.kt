package ru.hollowhorizon.hc.common.objects.entities.data

import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.IDataSerializer
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.serialize

@JvmField
val ANIMATION_MANAGER: IDataSerializer<GLTFAnimationManager> = object : IDataSerializer<GLTFAnimationManager> {
    override fun write(buffer: PacketBuffer, manager: GLTFAnimationManager) {
        buffer.writeNbt(NBTFormat.serialize(manager) as CompoundNBT)
    }

    override fun read(p_187159_1_: PacketBuffer): GLTFAnimationManager {
        return NBTFormat.deserialize(p_187159_1_.readNbt()!!)
    }

    override fun copy(manager: GLTFAnimationManager): GLTFAnimationManager {
        return manager
    }
}