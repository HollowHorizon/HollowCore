package ru.hollowhorizon.hc.client.handlers;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.client.render.shader.UniformCache;
import ru.hollowhorizon.hc.common.registry.ModShaders;


public class ShaderHandler {
    public static void renderShader() {
        UniformCache cache = ModShaders.squareOverlay.pushCache();
        ModShaders.squareOverlay.use();
        ModShaders.squareOverlay.popCache(cache);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.vertex(-1.0, 1.0, 1.0).endVertex();
        buffer.vertex(-1.0, -1.0, 1.0).endVertex();
        buffer.vertex(1.0, -1.0, 1.0).endVertex();

        tessellator.end();

        ModShaders.squareOverlay.release();
    }


}
