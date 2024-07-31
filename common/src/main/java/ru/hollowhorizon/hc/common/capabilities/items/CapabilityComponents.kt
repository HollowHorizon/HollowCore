package ru.hollowhorizon.hc.common.capabilities.items

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.ICapabilityDispatcher
import ru.hollowhorizon.hc.api.deserializeCapabilities
import ru.hollowhorizon.hc.api.serializeCapabilities
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.registry.HollowRegistry
import ru.hollowhorizon.hc.common.registry.RegistryObject
//
//object CapabilityComponents : HollowRegistry() {
//    val CAPABILITIES: RegistryObject<DataComponentType<ICapabilityDispatcher>> by register("hollowcore:capabilities".rl) {
//        DataComponentType.Builder<ICapabilityDispatcher>()
//            .persistent(CODEC)
//            .networkSynchronized(NETWORK_CODEC)
//            .cacheEncoding()
//            .build()
//    }
//
//    val CODEC: Codec<ICapabilityDispatcher> = RecordCodecBuilder.create { builder ->
//        builder.group(
//            CompoundTag.CODEC.fieldOf("nbt").forGetter { disp ->
//                val tag = CompoundTag()
//                disp.serializeCapabilities(tag)
//                tag
//            }
//        ).apply(builder, ::DefaultDispatcher)
//    }
//
//    val NETWORK_CODEC: StreamCodec<FriendlyByteBuf, ICapabilityDispatcher> = StreamCodec.ofMember(
//        { dispatcher, buffer ->
//            val tag = CompoundTag()
//            dispatcher.serializeCapabilities(tag)
//            buffer.writeNbt(tag)
//        },
//        { buffer ->
//            try {
//                return@ofMember DefaultDispatcher(buffer.readNbt() ?: throw IllegalStateException("NBT is null"))
//            } catch (e: Exception) {
//                // Без этого эта ошибка затеряется фиг пойми где, а так будет хоть какая-то информация
//                HollowCore.LOGGER.error("Can't decode capability packet!", e)
//                throw e
//            }
//        }
//    )
//}
//
//class DefaultDispatcher @JvmOverloads constructor(nbt: CompoundTag = CompoundTag()) : ICapabilityDispatcher {
//    override val capabilities = arrayListOf<CapabilityInstance>()
//
//    init {
//        deserializeCapabilities(nbt)
//    }
//}
