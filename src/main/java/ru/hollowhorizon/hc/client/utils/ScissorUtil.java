package ru.hollowhorizon.hc.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorUtil {
    private static final Deque<ScissorBox> STACK = new ArrayDeque<>();

    public static void start(int x, int y, int width, int height) {
        push();
        ScissorBox.fromScreenSpace(x, y, width, height).clampInside(STACK.peek()).apply();
    }

    public static void stop() {
        RenderSystem.disableScissor();
        pop();
    }

    public static void push() {
        if (GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
            final int[] raw = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, raw);
            STACK.push(new ScissorBox(raw[0], raw[1], raw[2], raw[3]));
            RenderSystem.disableScissor();
        }
    }

    public static void pop() {
        if (!STACK.isEmpty()) {
            STACK.pop().apply();
        }
    }

    private static final class ScissorBox {
        private static final ScissorBox INVALID = new ScissorBox(0, 0, 0, 0);
        public final int left;
        public final int bottom;
        public final int width;
        public final int height;

        private ScissorBox(int left, int bottom, int width, int height) {
            this.left = left;
            this.bottom = bottom;
            this.width = width;
            this.height = height;
        }

        public static ScissorBox fromScreenSpace(int x, int y, int width, int height) {
            var window = Minecraft.getInstance().getWindow();
            final double scale = window.getGuiScale();
            return new ScissorBox(
                    (int) (x * scale),
                    (int) ((window.getGuiScaledHeight() - y - height) * scale),
                    (int) (width * scale),
                    (int) (height * scale));
        }

        public void apply() {
            if (this != INVALID) {
                RenderSystem.enableScissor(left, bottom, width, height);
            }
        }

        private int minX() {
            return left;
        }

        private int maxX() {
            return left + width;
        }

        private int minY() {
            return bottom;
        }

        private int maxY() {
            return bottom + height;
        }

        public ScissorBox clampInside(@Nullable ScissorBox other) {
            if (other != null) {
                final int minX = Math.max(this.minX(), other.minX());
                final int maxX = Math.min(this.maxX(), other.maxX());
                final int minY = Math.max(this.minY(), other.minY());
                final int maxY = Math.min(this.maxY(), other.maxY());
                if (maxX > minX && maxY > minY) {
                    return new ScissorBox(minX, minY, maxX - minX, maxY - minY);
                }
                return INVALID;
            }
            return this;
        }
    }
}
