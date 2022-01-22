package ru.hollowhorizon.hc.client.render.mmd;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.dll.HollowRenderManager;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MMDTextureManager {
    static HollowRenderManager nf;
    static Map<String, Texture> texs;

    public static void init() {
        nf = HollowRenderManager.getInstance();
        texs = new HashMap<>();
    }

    public static Texture getTexture(String filename) {
        Texture result = texs.get(filename);
        if (result == null) {
            long nfTex = nf.LoadTexture(filename);
            if (nfTex == 0) {
                HollowCore.LOGGER.info(String.format("Cannot find texture: %s", filename));
                return null;
            }
            int x = nf.GetTextureX(nfTex);
            int y = nf.GetTextureY(nfTex);
            long texData = nf.GetTextureData(nfTex);
            boolean hasAlpha = nf.TextureHasAlpha(nfTex);

            int tex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
            int texSize = x * y * (hasAlpha ? 4 : 3);
            ByteBuffer texBuffer = ByteBuffer.allocateDirect(texSize);

            nf.CopyDataToByteBuffer(texBuffer, texData, texSize);
            if (hasAlpha) {
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, x, y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texBuffer);
            } else {
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, x, y, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, texBuffer);
            }
            nf.DeleteTexture(nfTex);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            result = new Texture();
            result.tex = tex;
            result.hasAlpha = hasAlpha;
            texs.put(filename, result);
        }
        return result;
    }

    public static void DeleteAll() {
        for (Texture i : texs.values()) {
            GL11.glDeleteTextures(i.tex);
        }
        texs = new HashMap<>();
    }

    public static class Texture {
        public int tex;
        public boolean hasAlpha;
    }
}
