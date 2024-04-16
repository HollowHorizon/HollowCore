/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.mixins.ftbteams;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamBase;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;

@Mixin(value = Team.class, remap = false)
public abstract class TeamMixin extends TeamBase {
    @Inject(method = "serializeNBT", at = @At("RETURN"))
    private void injectOnSave(CallbackInfoReturnable<SNBTCompoundTag> cir) {
        HollowCore.LOGGER.info("saving team capabilities!");

        CompoundTag tag = cir.getReturnValue();
        var capabilities = ((ICapabilityDispatcher) this).getCapabilities();
        if (capabilities != null) tag.put("hc_caps", capabilities.serializeNBT());
    }

    @Inject(method = "deserializeNBT", at = @At("RETURN"))
    private void injectOnLoad(CompoundTag tag, CallbackInfo ci) {
        var capabilities = ((ICapabilityDispatcher) this).getCapabilities();
        if (capabilities != null) capabilities.deserializeNBT(tag.getCompound("hc_caps"));
    }
}
