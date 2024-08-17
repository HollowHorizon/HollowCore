//? if fabric {
package ru.hollowhorizon.hc.fabric.internal

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

fun <T : HollowPacketV3<T>> registerPacket(type: Class<T>) {
    val annotation = type.getAnnotation(HollowPacketV2::class.java)
    val location = CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(MODID, type.name.lowercase().replace("\$", ".")))

    val codec: StreamCodec<RegistryFriendlyByteBuf, T> = CustomPacketPayload.codec(
        { packet, buffer ->
            buffer.writeNbt(NBTFormat.serializeNoInline(packet, type))
        },
        { buffer ->
            try {
                val tag = buffer.readNbt() ?: throw IllegalStateException("NBT is null")
                return@codec NBTFormat.deserializeNoInline(tag, type)
            } catch (e: Exception) {
                // Без этого эта ошибка затеряется фиг пойми где, а так будет хоть какая-то информация
                HollowCore.LOGGER.error("Can't decode ${type.name} packet!", e)
                throw e
            }
        }
    )
    when (annotation.toTarget) {
        HollowPacketV2.Direction.TO_CLIENT -> {
            PayloadTypeRegistry.playS2C()
                .register(location, codec)
            ClientPlayNetworking.registerGlobalReceiver(location) { payload: T, context: ClientPlayNetworking.Context ->
                payload.handle(context.player())
            }
        }

        HollowPacketV2.Direction.TO_SERVER -> {
            PayloadTypeRegistry.playC2S()
                .register(location, codec)
            ServerPlayNetworking.registerGlobalReceiver(location) { payload: T, context: ServerPlayNetworking.Context ->
                payload.handle(context.player())
            }
        }

        HollowPacketV2.Direction.ANY -> {
            PayloadTypeRegistry.playC2S()
                .register(location, codec)
            PayloadTypeRegistry.playS2C()
                .register(location, codec)
            ServerPlayNetworking.registerGlobalReceiver(location) { payload: T, context: ServerPlayNetworking.Context ->
                payload.handle(context.player())
            }
            ClientPlayNetworking.registerGlobalReceiver(location) { payload: T, context: ClientPlayNetworking.Context ->
                payload.handle(context.player())
            }
        }
    }
}
//?}