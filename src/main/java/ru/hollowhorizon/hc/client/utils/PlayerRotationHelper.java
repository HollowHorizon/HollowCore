package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;

import java.util.Objects;

public class PlayerRotationHelper {
    private static final ClientPlayerEntity PLAYER = Objects.requireNonNull(Minecraft.getInstance().player);
    private static float prevRenderYawOffset;
    private static float renderYawOffset;
    private static float prevRotationYaw;
    private static float rotationYaw;
    private static float prevRotationPitch;
    private static float rotationPitch;
    private static float prevRotationYawHead;
    private static float rotationYawHead;

    public static void save() {
        prevRenderYawOffset = PLAYER.yBodyRotO;
        renderYawOffset = PLAYER.yBodyRot;
        prevRotationYaw = PLAYER.yRotO;
        rotationYaw = PLAYER.yRot;
        prevRotationPitch = PLAYER.xRotO;
        rotationPitch = PLAYER.xRot;
        prevRotationYawHead = PLAYER.yHeadRotO;
        rotationYawHead = PLAYER.yHeadRot;
    }

    public static void clear() {
        PLAYER.yBodyRotO = 0.0F;
        PLAYER.yBodyRot = 0.0F;
        PLAYER.yRotO = 0.0F;
        PLAYER.yRot = 0.0F;
        PLAYER.xRotO = 0.0F;
        PLAYER.xRot = 0.0F;
        PLAYER.yHeadRotO = 0.0F;
        PLAYER.yHeadRot = 0.0F;
    }

    public static void load() {
        PLAYER.yBodyRotO = prevRenderYawOffset;
        PLAYER.yBodyRot = renderYawOffset;
        PLAYER.yRotO = prevRotationYaw;
        PLAYER.yRot = rotationYaw;
        PLAYER.xRotO = prevRotationPitch;
        PLAYER.xRot = rotationPitch;
        PLAYER.yHeadRotO = prevRotationYawHead;
        PLAYER.yHeadRot = rotationYawHead;
    }
}
