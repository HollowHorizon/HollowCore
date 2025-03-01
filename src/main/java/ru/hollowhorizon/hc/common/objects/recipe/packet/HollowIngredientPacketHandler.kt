package ru.hollowhorizon.hc.common.objects.recipe.packet

//? if fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import java.util.concurrent.CompletableFuture
//?} elif forge {
/*import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.forge.internal.ForgeNetworkHelper
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent
import java.util.function.Supplier
*///?}
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.recipe.HollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.deep.SupportedIngredientsPacketEncoder

object HollowIngredientPacketHandler {
    @JvmField val PACKET_ID = "hollowcore:hollow_ingredient_sync".rl
    @JvmField val SUPPORTED_INGREDIENTS: ThreadLocal<Set<ResourceLocation>> = ThreadLocal()
    const val PROTOCOL_VERSION_4 = 4

    fun init() {}

    //? if fabric {
    fun onClientReceiver() {
        ClientLoginNetworking.registerGlobalReceiver(PACKET_ID) { _, _, buf, _ ->
            val protocolVersion = buf.readVarInt()
            CompletableFuture.completedFuture(this.createResponse(protocolVersion))
        }
    }

    fun onServerReceiver() {
        ServerLoginConnectionEvents.QUERY_START.register { _, _, send, _ ->
            val buf = FriendlyByteBuf(Unpooled.buffer())
            buf.writeVarInt(PROTOCOL_VERSION_4)
            send.sendPacket(PACKET_ID, buf)
        }

        ServerLoginNetworking.registerGlobalReceiver(PACKET_ID) { s, h, received, buf, sync, res ->
            if (!received) return@registerGlobalReceiver

            val supported = decodeResponse(buf)
            val packetEncoder = h.connection.channel.pipeline().get("encoder")

            if (packetEncoder != null) {
                (packetEncoder as SupportedIngredientsPacketEncoder).hcSetSupportedIngredients(supported)
            }
        }
    }
    //?} elif forge {
    /*@JvmStatic
    @SubscribeEvent
    fun onPlayerJoin(e: PlayerEvent.Join) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeVarInt(PROTOCOL_VERSION_4)

        if (!e.player.level().isClientSide)
            ForgeNetworkHelper.hollowCoreChannel.send(PacketDistributor.PLAYER.with { e.player as ServerPlayer }, ServerSync(buf))
        else
            ForgeNetworkHelper.hollowCoreChannel.send(PacketDistributor.SERVER.noArg(), ClientSync(PROTOCOL_VERSION_4))
    }

    class ClientSync(private val protVer: Int) {
        constructor(buf: FriendlyByteBuf): this(buf.readVarInt())

        fun encode(buf: FriendlyByteBuf) {
            buf.writeVarInt(this.protVer)
        }

        fun handle(ctx: Supplier<NetworkEvent.Context>) {
            ctx.get().enqueueWork {
                val response = createResponse(this.protVer)
                if (response != null)
                    ForgeNetworkHelper.hollowCoreChannel.send(PacketDistributor.PLAYER.with { ctx.get().sender }, ServerSync(response))
            }
            ctx.get().packetHandled = true
        }
    }

    class ServerSync(private val data: FriendlyByteBuf) {
        fun encode(buf: FriendlyByteBuf) {
            buf.writeBytes(buf)
        }

        fun handle(ctx: Supplier<NetworkEvent.Context>) {
            ctx.get().enqueueWork {
                val supported = decodeResponse(data)
                val packetEncoder = ctx.get().networkManager.channel.pipeline().get("encode")
                if (packetEncoder != null)
                    (packetEncoder as SupportedIngredientsPacketEncoder).hcSetSupportedIngredients(supported)
            }

            ctx.get().packetHandled = true
        }
    }
    *///?}

    private fun createResponse(protVer: Int): FriendlyByteBuf? {
        if (protVer < PROTOCOL_VERSION_4) return null
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeVarInt(PROTOCOL_VERSION_4)
        buf.writeCollection(HollowIngredient.registeredSerializers.keys, FriendlyByteBuf::writeResourceLocation)
        return buf
    }

    private fun decodeResponse(buf: FriendlyByteBuf): Set<ResourceLocation> = when (val protVer = buf.readVarInt()) {
        PROTOCOL_VERSION_4 -> {
            buf.readCollection(::HashSet, FriendlyByteBuf::readResourceLocation).apply {
                removeIf { !HollowIngredient.registeredSerializers.containsKey(it) }
            }
        }
        else -> throw IllegalArgumentException("Unknown ingredient sync protocol version: $protVer")
    }
}