package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
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

    public static BufferedImage getImage(ResourceLocation resLoc) throws IOException {
        try {
            InputStream in = Minecraft.getInstance().getResourceManager().getResource(resLoc).getInputStream();
            return ImageIO.read(in);
        } catch (IOException var2) {
            throw new IOException(String.format("There was an error loading the ResourceLocation \"%s\".", resLoc));
        }
    }

    public static BufferedImage getImage(String fileLoc) throws IOException {
        InputStream in = null;

        try {
            fileLoc = fileLoc.replaceAll("//", "/");
            in = ImageHelper.class.getResourceAsStream(fileLoc);
            return ImageIO.read(in);
        } catch (IOException var3) {

            throw new IOException(String.format("There was an error loading the file \"%s\".", fileLoc));
        }
    }

    public static BufferedImage rotateImg(BufferedImage img, double rotation) {
        double midX = (double)img.getWidth() * 0.5D;
        double midY = (double)img.getHeight() * 0.5D;
        double radians = Math.toRadians(rotation);
        AffineTransform trans = new AffineTransform();
        trans.rotate(radians, midX, midY);
        AffineTransformOp op = new AffineTransformOp(trans, 1);
        return op.filter(img, null);
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
