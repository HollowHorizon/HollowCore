package ru.hollowhorizon.hc.common.capabilities.items

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
