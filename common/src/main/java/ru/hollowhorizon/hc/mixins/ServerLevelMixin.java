package ru.hollowhorizon.hc.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.level.LevelEvent;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "save", at = @At("TAIL"))
    private void onSave(ProgressListener $$0, boolean $$1, boolean $$2, CallbackInfo ci) {
        EventBus.post(new LevelEvent.Save(JavaHacks.forceCast(this)));
    }


}
