package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.KeyValueStore;
import de.fabmax.kool.KoolContext;
import de.fabmax.kool.editor.PlatformFunctions;
import de.fabmax.kool.editor.WindowButtonStyle;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;

@Mixin(value = PlatformFunctions.class, remap = false)
public class PlatformFunctionsMixin {
    @Inject(method = "getWindowButtonStyle", at = @At("HEAD"), cancellable = true)
    private void onGetWindowButtonStyle(CallbackInfoReturnable<WindowButtonStyle> cir) {
        cir.setReturnValue(WindowButtonStyle.NONE);
    }

    @Inject(method = "isWindowMaximized", at = @At("HEAD"), cancellable = true)
    private void onGetIsWindowMaximized(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(glfwGetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(), GLFW_MAXIMIZED) != 0);
    }

    @Inject(method = "onEditorStarted", at = @At("HEAD"), cancellable = true)
    private void onStarted(KoolContext ctx, CallbackInfo ci) {
        var posX = KeyValueStore.INSTANCE.getInt("editor.window.posX", -1);
        var posY = KeyValueStore.INSTANCE.getInt("editor.window.posY", -1);
        var handle = Minecraft.getInstance().getWindow().getWindow();

        if (posX != -1 && posY != -1) {
            GLFW.glfwSetWindowPos(handle, posX, posY);
        }

        var isMaximized = KeyValueStore.INSTANCE.getBoolean("editor.window.isMaximized", false);
        if (isMaximized) {
            GLFW.glfwMaximizeWindow(handle);
        }
        ci.cancel();
    }

    @Inject(method = "onExit", at = @At("HEAD"), cancellable = true)
    private void onExit(KoolContext ctx, CallbackInfo ci) {
        var wnd = Minecraft.getInstance().getWindow();

        if (glfwGetWindowAttrib(wnd.getWindow(), GLFW_MAXIMIZED) != 0) {
            KeyValueStore.INSTANCE.setBoolean("editor.window.isMaximized", true);
        } else {
            KeyValueStore.INSTANCE.setBoolean("editor.window.isMaximized", false);
            KeyValueStore.INSTANCE.setInt("editor.window.posX", wnd.getX());
            KeyValueStore.INSTANCE.setInt("editor.window.posY", wnd.getY());
            KeyValueStore.INSTANCE.setInt("editor.window.width", wnd.getWidth());
            KeyValueStore.INSTANCE.setInt("editor.window.height", wnd.getHeight());
        }
        ci.cancel();
    }

    @Inject(method = "toggleMaximizeWindow", at = @At("HEAD"), cancellable = true)
    private void onToggleMaximizeWindow(CallbackInfo ci) {
        ci.cancel();
        var handle = Minecraft.getInstance().getWindow().getWindow();
        if (glfwGetWindowAttrib(handle, GLFW_MAXIMIZED) != 0) {
            GLFW.glfwRestoreWindow(handle);
        } else {
            GLFW.glfwMaximizeWindow(handle);
        }
    }

    @Inject(method = "minimizeWindow", at = @At("HEAD"), cancellable = true)
    private void onMinimizeWindow(CallbackInfo ci) {
        ci.cancel();
        var handle = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwIconifyWindow(handle);
    }

    @Inject(method = "closeWindow", at = @At("HEAD"), cancellable = true)
    private void onClose(CallbackInfo ci) {
        ci.cancel();
        Minecraft.getInstance().close();
    }
}
