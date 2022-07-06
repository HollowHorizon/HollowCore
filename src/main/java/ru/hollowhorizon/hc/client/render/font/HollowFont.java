package ru.hollowhorizon.hc.client.render.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowFont {

    float imgSize = 512;
    CharData[] charData = new CharData[256];
    Font font;
    boolean antiAlias, fractionalMetrics;
    int fontHeight = -1, charOffset = 0;
    public DynamicTexture tex;

    public static void main(String[] args) {
        Font font = getFont("ubuntu.ttf", 10);
        new HollowFont(font, true, true);
    }

    private static Font getFont(String location, int size) {
        Font font;

        try {
            InputStream is = HollowJavaUtils.getResource(new ResourceLocation(MODID, "fonts/" + location));
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error! Font can't be loaded.");
            font = new Font("default", Font.PLAIN, +10);
        }

        return font;
    }

    public HollowFont(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        this.tex = this.setupTexture(font, antiAlias, fractionalMetrics, this.charData);
        this.tex.upload();
    }

    protected DynamicTexture setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage img = this.generateFontImage(font, antiAlias, fractionalMetrics, chars);

        File outputfile = new File("image.jpg");
        try {
            ImageIO.write(img, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return new DynamicTexture(ImageHelper.getNativeImage(img));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        int imgSize = (int) this.imgSize;
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, 2);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, imgSize, imgSize);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int charHeight = 0;
        int positionX = 0;
        int positionY = 1;
        int index = 0;

        while (index < chars.length) {
            char c = (char) index;
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(c), graphics);
            charData.width = dimensions.getBounds().width + 8;
            charData.height = dimensions.getBounds().height;

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > this.fontHeight) {
                this.fontHeight = charData.height;
            }

            chars[index] = charData;
            graphics.drawString(String.valueOf(c), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
            ++index;
        }

        return bufferedImage;
    }

    public void drawChar(MatrixStack stack, CharData[] chars, char c, float x, float y) throws ArrayIndexOutOfBoundsException {
        try {
            this.drawQuad(stack, x, y, chars[c].width, chars[c].height, chars[c].storedX, chars[c].storedY, chars[c].width, chars[c].height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawQuad(MatrixStack stack, float x2, float y2, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight) {
        Matrix4f mat = stack.last().pose();
        float renderSRCX = srcX / this.imgSize;
        float renderSRCY = srcY / this.imgSize;
        float renderSRCWidth = srcWidth / this.imgSize;
        float renderSRCHeight = srcHeight / this.imgSize;
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(mat, x2 + width, y2, 50).uv(renderSRCX + renderSRCWidth, renderSRCY).endVertex();
        builder.vertex(mat, x2, y2, 50).uv(renderSRCX, renderSRCY).endVertex();
        builder.vertex(mat, x2, y2 + height, 50).uv(renderSRCX, renderSRCY + renderSRCHeight).endVertex();

        builder.vertex(mat, x2, y2 + height, 50).uv(renderSRCX, renderSRCY + renderSRCHeight).endVertex();
        builder.vertex(mat, x2 + width, y2 + height, 50).uv(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight).endVertex();
        builder.vertex(mat, x2 + width, y2, 50).uv(renderSRCX + renderSRCWidth, renderSRCY).endVertex();

        Tessellator.getInstance().end();
    }

    public void setAntiAlias(boolean antiAlias) {
        if (this.antiAlias != antiAlias) {
            this.antiAlias = antiAlias;
            this.tex = this.setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData);
        }
    }

    public boolean isFractionalMetrics() {
        return this.fractionalMetrics;
    }

    public void setFractionalMetrics(boolean fractionalMetrics) {
        if (this.fractionalMetrics != fractionalMetrics) {
            this.fractionalMetrics = fractionalMetrics;
            this.tex = this.setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData);
        }
    }

    public void setFont(Font font) {
        this.font = font;
        this.tex = this.setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData);
    }

    protected static class CharData {
        public int width, height,
                storedX, storedY;

        protected CharData() {
        }
    }
}
