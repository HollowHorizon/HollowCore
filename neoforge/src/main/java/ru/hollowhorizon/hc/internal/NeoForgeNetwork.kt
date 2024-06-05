package ru.hollowhorizon.hc.internal

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

fun <T : HollowPacketV3<T>> registerPacket(registerer: PayloadRegistrar, type: Class<T>) {
    val annotation = type.getAnnotation(HollowPacketV2::class.java)
    val location = CustomPacketPayload.Type<T>(ResourceLocation(MODID, type.name.lowercase()))

    val codec: StreamCodec<FriendlyByteBuf, T> = CustomPacketPayload.codec(
        { packet, buffer ->
            val tag = NBTFormat.serializeNoInline(packet, type)
            if (tag is CompoundTag) buffer.writeNbt(tag)
            else buffer.writeNbt(CompoundTag().apply { put("data", tag) })
        },
        { buffer ->
            try {
                val tag = buffer.readNbt() ?: throw IllegalStateException("NBT is null")
                if (tag.contains("data")) NBTFormat.deserializeNoInline(tag.get("%%data")!!, type)
                else NBTFormat.deserializeNoInline(tag, type)
            } catch (e: Exception) {
                // Без этого эта ошибка затеряется фиг пойми где, а так будет хоть какая-то информация
                HollowCore.LOGGER.error("Can't decode ${type.name} packet!", e)
                throw e
            }
        }
    )

    when (annotation.toTarget) {
        HollowPacketV2.Direction.TO_CLIENT -> {
            registerer.playToClient(location, codec) { payload, context ->
                payload.handle(context.player())
            }
        }

        HollowPacketV2.Direction.TO_SERVER -> {
            registerer.playToServer(location, codec) { payload, context ->
                payload.handle(context.player())
            }
        }

        HollowPacketV2.Direction.ANY -> {
            registerer.playBidirectional(location, codec) { payload, context ->
                payload.handle(context.player())
            }
        }
    }
}