package ru.hollowhorizon.hc.client.model.smd;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public enum EnumMaterialOption {
    NO_LIGHTING() {
        public void begin(Object... params) {
            FS.start_unlit();
        }

        public void end(Object... params) {
            FS.end_unlit();
        }
    },
    WIREFRAME {
        public void begin(Object... params) {
            FS.start_wire();
        }

        public void end(Object... params) {
            GL11.glPopAttrib();
        }
    },
    TRANSPARENCY {
        public void begin(Object... params) {
            FS.start_transparency(params);
        }

        public void end(Object... params) {
            FS.end_transparency();
        }
    },
    NOCULL {
        public void begin(Object... params) {
            FS.enable_nocull();
        }

        public void end(Object... params) {
            FS.disable_nocull();
        }
    };

    public static int cubemapID = 33986;

    private EnumMaterialOption() {
    }

    public abstract void begin(Object... var1);

    public abstract void end(Object... var1);

    public static final class FS {
        private FS() {
        }

        public static void start_wire() {
            GL11.glPushAttrib(2880);
            GL11.glPolygonMode(1032, 6913);
        }

        public static void start_unlit() {
            RenderSystem.disableLighting();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        }

        public static void end_unlit() {
            RenderSystem.enableLighting();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        }

        public static void start_transparency(Object[] actuallyAFloat) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            if (actuallyAFloat != null && actuallyAFloat.length > 0 && actuallyAFloat[0] instanceof Float) {
                float alpha = (Float) actuallyAFloat[0];
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            }

        }

        public static void end_transparency() {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        public static void enable_nocull() {
            RenderSystem.enableCull();
        }

        public static void disable_nocull() {
            RenderSystem.disableCull();
        }
    }
}
