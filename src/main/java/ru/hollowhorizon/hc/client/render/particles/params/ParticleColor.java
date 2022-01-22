package ru.hollowhorizon.hc.client.render.particles.params;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class ParticleColor {
    private final float r;
    private final float g;
    private final float b;
    private final int color;

    public ParticleColor(final int r, final int g, final int b) {
        this.r = r / 255.0f;
        this.g = g / 255.0f;
        this.b = b / 255.0f;
        this.color = (r << 16 | g << 8 | b);
    }

    public static ParticleColor makeRandomColor(final int r, final int g, final int b, final Random random) {
        return new ParticleColor(random.nextInt(r), random.nextInt(g), random.nextInt(b));
    }

    public ParticleColor(final float r, final float g, final float b) {
        this((int)r, (int)g, (int)b);
    }

    public static ParticleColor fromInt(final int color) {
        final int r = color >> 16 & 0xFF;
        final int g = color >> 8 & 0xFF;
        final int b = color & 0xFF;
        return new ParticleColor(r, g, b);
    }

    public float getRed() {
        return this.r;
    }

    public float getGreen() {
        return this.g;
    }

    public float getBlue() {
        return this.b;
    }

    public int getColor() {
        return this.color;
    }

    public String serialize() {
        return "" + this.r + "," + this.g + "," + this.b;
    }

    public ParticleColor.IntWrapper toWrapper() {
        return new ParticleColor.IntWrapper(this);
    }

    @Nullable
    public static ParticleColor deserialize(final String string) {
        if (string == null) {
            return null;
        }
        final String[] arr = string.split(",");
        return new ParticleColor(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()), Integer.parseInt(arr[2].trim()));
    }

    public static class IntWrapper
    {
        public int r;
        public int g;
        public int b;

        public IntWrapper(final int r, final int g, final int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public IntWrapper(final ParticleColor color) {
            this.r = (int)(color.getRed() * 255.0);
            this.g = (int)(color.getGreen() * 255.0);
            this.b = (int)(color.getBlue() * 255.0);
        }

        public ParticleColor toParticleColor() {
            return new ParticleColor(this.r, this.g, this.b);
        }

        public String serialize() {
            return "" + this.r + "," + this.g + "," + this.b;
        }

        public void makeVisible() {
            if (this.r + this.g + this.b < 20) {
                this.b += 10;
                this.g += 10;
                this.r += 10;
            }
        }

        @Nonnull
        public static IntWrapper deserialize(final String string) {
            IntWrapper color = defaultParticleColorWrapper();
            try {
                final String[] arr = string.split(",");
                color = new IntWrapper(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()), Integer.parseInt(arr[2].trim()));
                return color;
            }
            catch (Exception e) {
                e.printStackTrace();
                return color;
            }
        }

        public static IntWrapper defaultParticleColorWrapper() {
            return new IntWrapper(255, 25, 180);
        }
    }
}