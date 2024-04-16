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

package ru.hollowhorizon.hc.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.hollowhorizon.hc.common.capabilities.ICapabilitySyncer;
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater;

import javax.annotation.Nullable;

@Mixin(value = CapabilityProvider.class, remap = false)
public abstract class CapabilityProviderMixin implements ICapabilityUpdater {
    @Shadow
    @Nullable
    protected abstract CapabilityDispatcher getCapabilities();

    @Shadow
    private boolean valid;

    @Override
    public void updateCapability(@NotNull Capability<?> capability, @NotNull Tag newValue) {
        final CapabilityDispatcher disp = getCapabilities();

        if (valid && disp != null) {
            CompoundTag nbt = new CompoundTag();
            String name = capability.getName();
            nbt.put("hc_capabilities:" + name.toLowerCase(), newValue);
            disp.deserializeNBT(nbt);
        }

        if (this instanceof ICapabilitySyncer syncer) {
            syncer.onCapabilitySync(capability);
        }
    }
}
