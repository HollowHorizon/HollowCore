package ru.hollowhorizon.hc.client.render.game;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;

public class HollowTextureManager {
    public static void register(DynamicTexture texture, String fileName) {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        ResourceLocation loc = new ResourceLocation("hc", "generated/"+fileName);
        tm.register(loc, texture);

    }

    public static void register(InputStream is, String fileName) {
        try {
            register(new DynamicTexture(NativeImage.read(is)), fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void register(File file) throws FileNotFoundException {
        register(new FileInputStream(file), file.getName());
    }
}
