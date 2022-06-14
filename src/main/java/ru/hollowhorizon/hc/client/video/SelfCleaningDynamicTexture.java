package ru.hollowhorizon.hc.client.video;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import ru.hollowhorizon.hc.HollowCore;

import javax.annotation.Nonnull;

public class SelfCleaningDynamicTexture extends DynamicTexture {
    public SelfCleaningDynamicTexture(NativeImage nativeImage) {
        super(nativeImage);
    }

    @Override
    public void setPixels(@Nonnull NativeImage nativeImage) {
        super.setPixels(nativeImage);
        if (this.getPixels() != null) {
            TextureUtil.prepareImage(this.getId(), this.getPixels().getWidth(), this.getPixels().getHeight());
            this.upload();
        } else {
            HollowCore.LOGGER.error("Called setPixels in {} with NativeImage.getPixels == null", this.getClass().getName());
        }
    }

}
