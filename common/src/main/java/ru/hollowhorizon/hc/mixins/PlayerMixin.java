package ru.hollowhorizon.hc.mixins;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.entity.player.PlayerInteractEvent;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void onInteract(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        var event = new PlayerInteractEvent.EntityInteract(JavaHacks.forceCast(this), hand, entityToInteractOn);
        EventBus.post(event);
        if (event.isCanceled()) cir.setReturnValue(InteractionResult.PASS);
    }
}
