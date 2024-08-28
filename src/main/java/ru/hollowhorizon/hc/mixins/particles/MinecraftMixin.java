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

package ru.hollowhorizon.hc.mixins.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.imgui.ImGuiExtensionsKt;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void resizeCapturedDepthBuffer(CallbackInfo ci) {
        RenderSystem.recordRenderCall(() -> {
            final var window = Minecraft.getInstance().getWindow();
            RenderStateCapture.CAPTURED_WORLD_DEPTH_BUFFER.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            RenderStateCapture.CAPTURED_HAND_DEPTH_BUFFER.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            ImGuiExtensionsKt.getImguiWindowBuffer().resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            ImGuiExtensionsKt.getImguiBackgroundBuffer().resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            ImGuiExtensionsKt.getImguiForegroundBuffer().resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);

        });
    }
}