package ru.hollowhorizon.hc.client.models.core.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import ru.hollowhorizon.hc.HollowCore;

import java.lang.reflect.Field;

/**
 * Created by Jacob on 1/25/2020.
 */
public class Utils {


    public static void readLightTextureData() {
        LightTexture tex = Minecraft.getInstance().gameRenderer.lightTexture();

        Field privateField = null;
        try {
            privateField = LightTexture.class.getDeclaredField("field_205111_b");
            privateField.setAccessible(true);
            try {
                NativeImage image = (NativeImage) privateField.get(tex);
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        int color = image.getPixelRGBA(x, y);
                        int alpha = color >>> 24;
                        int red = color >>> 16 & 0xFF;
                        int green = color >>> 8 & 0xFF;
                        int blue = color & 0xFF;
                        HollowCore.LOGGER.info("Pixel {}, {} color: r: {} g: {} b: {} a: {}", x, y, red, green, blue, alpha);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


    }
}
