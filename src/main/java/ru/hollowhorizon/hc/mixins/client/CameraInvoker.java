package ru.hollowhorizon.hc.mixins.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraInvoker {
    @Invoker("setRotation")
    void rotate(float yaw, float pitch); // Method to rotate the camera by specified yaw and pitch angles
}
