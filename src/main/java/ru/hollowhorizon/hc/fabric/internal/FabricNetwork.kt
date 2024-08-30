package ru.hollowhorizon.hc.fabric.internal

//? if fabric && >=1.21 {

/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
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
import ru.hollowhorizon.hc.common.network.RequestPacket

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

*///?} elif fabric {

/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

val PACKETS = hashMapOf<Class<*>, PacketInfo>()

class PacketInfo(val direction: HollowPacketV2.Direction)

fun <T : HollowPacketV3<T>> registerPacket(type: Class<T>) {
    val annotation = type.getAnnotation(HollowPacketV2::class.java)
    val location = "$MODID:${type.name.lowercase().replace("\$", ".")}".rl


    val deserializer: (FriendlyByteBuf) -> T = { buffer ->
        try {
            val tag = buffer.readNbt() ?: throw IllegalStateException("NBT is null")
            NBTFormat.deserializeNoInline(tag, type)
        } catch (e: Exception) {
            // Без этого эта ошибка затеряется фиг пойми где, а так будет хоть какая-то информация
            HollowCore.LOGGER.error("Error while deserializing ${type.simpleName} packet", e)
            throw e
        }
    }


    when (annotation.toTarget) {
        HollowPacketV2.Direction.TO_CLIENT -> {
            ClientPlayNetworking.registerGlobalReceiver(
                location
            ) { client, handler, buf, responseSender ->
                val player = client.player ?: Minecraft.getInstance().player
                if (player == null) {
                    HollowCore.LOGGER.warn("No player found in minecraft... How do you receive that ${type.simpleName}?")
                    return@registerGlobalReceiver
                }
                deserializer(buf).handle(player)
            }
        }

        HollowPacketV2.Direction.TO_SERVER -> {
            ServerPlayNetworking.registerGlobalReceiver(
                location
            ) { server, player, handler, buf, responseSender ->
                deserializer(buf).handle(player)
            }
        }

        HollowPacketV2.Direction.ANY -> {
            ClientPlayNetworking.registerGlobalReceiver(
                location
            ) { client, handler, buf, responseSender ->
                val player = client.player
                if (player == null) {
                    HollowCore.LOGGER.error("No player found in minecraft... How do you receive that ${type.simpleName}?")
                    return@registerGlobalReceiver
                }
                deserializer(buf).handle(player)
            }
            ServerPlayNetworking.registerGlobalReceiver(
                location
            ) { server, player, handler, buf, responseSender ->
                deserializer(buf).handle(player)
            }
        }
    }
}
*///?}