package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HollowJavaUtils {

    @OnlyIn(Dist.CLIENT)
    public static InputStream getResource(ResourceLocation location) {
        try {
            return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
        } catch (Exception e) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/" + location.getNamespace() + "/" + location.getPath());
        }
    }

    public static boolean hasResource(ResourceLocation location) {
        try {
            getResource(location);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static <R, K extends R> K castDarkMagic(R original) {
        return (K) original;
    }


    public static void initPath(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            if (file.exists()) file.delete();

            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int getResourceLocationSize(ResourceLocation location) throws IOException {
        return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream().available();
    }

    public static int getInputStreamSize(InputStream inputStream) throws IOException {
        return inputStream.available();
    }
}
