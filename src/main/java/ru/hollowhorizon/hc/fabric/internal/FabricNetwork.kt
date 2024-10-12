package ru.hollowhorizon.hc.fabric.internal

//?} if fabric {

import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

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

    val isClient = FabricLoader.getInstance().environmentType == EnvType.CLIENT

    when (annotation.toTarget) {
        HollowPacketV2.Direction.TO_CLIENT -> {
            if (isClient) ClientPlayNetworking.registerGlobalReceiver(
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
            if (isClient) ClientPlayNetworking.registerGlobalReceiver(
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
//?}
