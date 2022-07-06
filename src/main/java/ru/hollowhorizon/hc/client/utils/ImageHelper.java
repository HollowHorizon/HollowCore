package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ImageHelper {
    private static final String LOAD_ERROR = "There was an error loading the %s \"%s\".";

    public ImageHelper() {
    }

    public static BufferedImage getImage(String fileLoc) throws IOException {
        InputStream in;

        try {
            fileLoc = fileLoc.replaceAll("//", "/");
            in = ImageHelper.class.getResourceAsStream(fileLoc);
            return ImageIO.read(in);
        } catch (IOException var3) {
            throw new IOException(String.format(LOAD_ERROR, "file", fileLoc));
        }
    }

    public static BufferedImage getImage(ResourceLocation resLoc) throws IOException {
        try {
            InputStream in = Minecraft.getInstance().getResourceManager().getResource(resLoc).getInputStream();
            return ImageIO.read(in);
        } catch (IOException var2) {
            throw new IOException(String.format(LOAD_ERROR, "ResourceLocation", resLoc));
        }
    }

    public static BufferedImage rotateImg(BufferedImage img, double rotation) {
        double midX = (double)img.getWidth() * 0.5D;
        double midY = (double)img.getHeight() * 0.5D;
        double radians = Math.toRadians(rotation);
        AffineTransform trans = new AffineTransform();
        trans.rotate(radians, midX, midY);
        AffineTransformOp op = new AffineTransformOp(trans, 1);
        return op.filter(img, (BufferedImage)null);
    }

    public static NativeImage getNativeImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, width, height, pixels, 0, width);
        NativeImage n = new NativeImage(img.getWidth(), img.getHeight(), true);

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                int pixel = pixels[y * width + x];
                n.setPixelRGBA(pixel >> 16 & 255, pixel >> 8 & 255, pixel & 255);
            }
        }
        return n;
    }

    public static ByteBuffer getBuffer(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, width, height, pixels, 0, width);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                int pixel = pixels[y * width + x];
                buffer.put((byte)(pixel >> 16 & 255));
                buffer.put((byte)(pixel >> 8 & 255));
                buffer.put((byte)(pixel & 255));
                buffer.put((byte)(pixel >> 24 & 255));
            }
        }

        buffer.flip();
        buffer.rewind();
        return buffer;
    }
}