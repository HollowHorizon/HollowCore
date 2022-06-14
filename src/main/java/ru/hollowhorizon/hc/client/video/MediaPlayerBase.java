package ru.hollowhorizon.hc.client.video;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.HollowCore;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class MediaPlayerBase extends AbstractMediaPlayer {
    public final DynamicResourceLocation dynamicResourceLocation;

    public final MediaPlayerCallback callback = new MediaPlayerCallback(0, this);

    public NativeImage image = new NativeImage(1,1, true);

    public SelfCleaningDynamicTexture dynamicTexture = new SelfCleaningDynamicTexture(image);

    public MediaPlayerBase(DynamicResourceLocation resourceLocation) {
        image = new NativeImage(1, 1, true);
        image.setPixelRGBA(0, 0, 0);
        dynamicTexture.setPixels(image);
        dynamicResourceLocation = resourceLocation;
        Minecraft.getInstance().getTextureManager().register(resourceLocation.toWorkingString().replace(':', '.'), dynamicTexture);
        HollowCore.LOGGER.debug("TextureLocation is '{}'", dynamicResourceLocation);
    }

    @Override
    public EmbeddedMediaPlayer api() {
        return null;
    }

    @Override
    public void markToRemove() {
        // Template methode.
    }

    @Override
    public void cleanup() {
        // Template methode.
    }

    public int[] getIntFrame() {
        return new int[0];
    }

    public int getWidth() {
        return 0; //NOSONAR
    }

    public IntegerBuffer2D getIntBuffer() {
        return new IntegerBuffer2D(1, 1);
    }

    public void setIntBuffer(IntegerBuffer2D in) {
        // Template methode.
    }

    public ResourceLocation renderToResourceLocation() {
        return dynamicResourceLocation;
    }
}
