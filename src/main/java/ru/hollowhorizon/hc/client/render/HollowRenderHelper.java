package ru.hollowhorizon.hc.client.render;

import net.minecraft.client.renderer.GLAllocation;

import java.nio.IntBuffer;

public class HollowRenderHelper {
    public static IntBuffer createDirectIntBuffer(int capacity)
    {
        return GLAllocation.createByteBuffer(capacity << 2).asIntBuffer();
    }
}
