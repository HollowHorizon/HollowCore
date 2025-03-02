package ru.hollowhorizon.hc.mixins;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.objects.recipe.deep.SupportedIngredientsPacketEncoder;
import ru.hollowhorizon.hc.common.objects.recipe.packet.HollowIngredientPacketHandler;

import java.util.Set;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin implements SupportedIngredientsPacketEncoder {
    @Unique
    private Set<ResourceLocation> hc$supportedIngredients = Set.of();

    @Override
    public void hc_SetSupportedIngredients(@NotNull Set<? extends ResourceLocation> value) {
        hc$supportedIngredients = JavaHacks.forceCast(value);
    }

    @Inject(
            method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
            )
    )
    private void handle(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        HollowIngredientPacketHandler.SUPPORTED_INGREDIENTS.set(hc$supportedIngredients);
    }

    @Inject(
            method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V",
                            shift = At.Shift.AFTER,
                            by = 1
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/network/protocol/Packet;isSkippable()Z"
                    )
            }
    )
    private void release(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        HollowIngredientPacketHandler.SUPPORTED_INGREDIENTS.set(null);
    }
}
