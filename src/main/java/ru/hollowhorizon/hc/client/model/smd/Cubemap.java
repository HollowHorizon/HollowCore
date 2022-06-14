package ru.hollowhorizon.hc.client.model.smd;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.client.utils.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class Cubemap extends SimpleTexture {
    public int sqr;
    public int width;
    public int height;
    public final ByteBuffer[] buffers = new ByteBuffer[6];
    protected static HashMap<ResourceLocation, Cubemap> cubemaps = new HashMap();
    public final ResourceLocation resourceLoc;

    public Cubemap(ResourceLocation resLoc) throws IOException {
        super(resLoc);
        this.resourceLoc = resLoc;

        BufferedImage fullImg = ImageHelper.getImage(resLoc);
        this.initBuffers(fullImg, resLoc.toString());
        cubemaps.put(resLoc, this);
    }

    protected void initBuffers(BufferedImage fullImg, String fileLoc) throws IOException {
        this.width = fullImg.getWidth();
        this.height = fullImg.getHeight();
        if ((double)this.height % 4.0D == 0.0D && (double)this.width % 3.0D == 0.0D && this.height / 4 == this.width / 3) {
            this.sqr = this.width / 3;
            BufferedImage subImg = null;

            for(int i = 0; i < 6; ++i) {
                switch(i) {
                    case 0:
                        subImg = ImageHelper.rotateImg(fullImg.getSubimage(2 * this.sqr, this.sqr, this.sqr, this.sqr), 90.0D);
                        break;
                    case 1:
                        subImg = ImageHelper.rotateImg(fullImg.getSubimage(0, this.sqr, this.sqr, this.sqr), -90.0D);
                        break;
                    case 2:
                        subImg = fullImg.getSubimage(this.sqr, this.sqr, this.sqr, this.sqr);
                        break;
                    case 3:
                        subImg = fullImg.getSubimage(this.sqr, 3 * this.sqr, this.sqr, this.sqr);
                        break;
                    case 4:
                        subImg = fullImg.getSubimage(this.sqr, 2 * this.sqr, this.sqr, this.sqr);
                        break;
                    case 5:
                        subImg = ImageHelper.rotateImg(fullImg.getSubimage(this.sqr, 0, this.sqr, this.sqr), 180.0D);
                }

                ByteBuffer buffer = ImageHelper.getBuffer(subImg);
                this.buffers[i] = buffer;
                GL11.glTexImage2D('蔕' + i, 0, 32856, this.sqr, this.sqr, 0, 6408, 5121, buffer);
            }

        } else {
            String dimError = "The Dimensions of %s are invalid! A Cubemap image must have an exact width-to-height ratio of 3:4!";
            throw new IOException(String.format(dimError, fileLoc));
        }
    }

    public static void begin(ResourceLocation resLoc) {
        if (Minecraft.useFancyGraphics()) {
            Cubemap map = (Cubemap)cubemaps.get(resLoc);
            if (map == null) {
                try {
                    map = new Cubemap(resLoc);
                } catch (IOException var3) {
                    var3.printStackTrace();
                }
            }

            map.start();
        }
    }

    protected void newBinding() {
        int bindID = this.getId();
        GL11.glBindTexture(34067, bindID);

        for(int i = 0; i < 6; ++i) {
            GL11.glTexImage2D('蔕' + i, 0, 32856, this.sqr, this.sqr, 0, 6408, 5121, this.buffers[i]);
        }

    }

    public void start() {
        RenderSystem.activeTexture(EnumMaterialOption.cubemapID);
        GL11.glEnable(3168);
        GL11.glEnable(3169);
        GL11.glEnable(3170);
        GL11.glEnable(34067);
        GL11.glEnable(2977);

        for(int i = 0; i < 6; ++i) {
            GL11.glTexImage2D('蔕' + i, 0, 32856, this.sqr, this.sqr, 0, 6408, 5121, this.buffers[i]);
        }

        GL11.glPixelStorei(3317, 1);
        GL11.glTexGeni(8192, 9472, 34066);
        GL11.glTexGeni(8193, 9472, 34066);
        GL11.glTexGeni(8194, 9472, 34066);
    }

    public void end() {
        GL11.glPixelStorei(3317, 1);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(34067);
        RenderSystem.activeTexture(33984);
    }

    public static void debugResourceStuff(ResourceLocation resLoc) {
        Field texManagerResManager = null;
        TextureManager texManager = Minecraft.getInstance().getTextureManager();
        IResourceManager resManager = null;
        IResource resource = null;
        BufferedImage img = null;

        try {
            texManagerResManager = TextureManager.class.getDeclaredField("field_110582_d");
            texManagerResManager.setAccessible(true);
            resManager = (IResourceManager)texManagerResManager.get(texManager);
            resource = resManager.getResource(resLoc);
            InputStream stream = resource.getInputStream();
            img = ImageIO.read(stream);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        new SimpleTexture(resLoc);
        System.out.println(img);
    }

    public String toString() {
        return "Cubemap(" + this.sqr + "x" + this.sqr + ";" + this.resourceLoc + ")";
    }

    static {
        GL11.glPixelStorei(3317, 1);
        GL11.glTexParameteri(34067, 10242, 33071);
        GL11.glTexParameteri(34067, 10243, 33071);
        GL11.glTexParameteri(34067, 32882, 33071);
        GL11.glTexParameteri(34067, 10240, 9728);
        GL11.glTexParameteri(34067, 10241, 9729);
    }
}

