package ru.hollowhorizon.hc.client.model.dae.model.skin;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skin.ISkin;
import ru.hollowhorizon.hc.client.model.dae.model.AnimatrixModel;
import ru.hollowhorizon.hc.client.render.game.VAO;

/**
 * Represents the Skin of a {@link AnimatrixModel}
 */
@OnlyIn(Dist.CLIENT)
public class AnimatrixSkin implements ISkin
{
    private final VAO              skinModel;
    private final ResourceLocation texture;

    public AnimatrixSkin(final VAO skinModel, final ResourceLocation texture) {
        this.skinModel = skinModel;
        this.texture = texture;
    }

    /**
     * Returns the {@link VAO} that represents the skin data on the GPU.
     *
     * @return The {@link VAO} containing the skin data on the GPU.
     */
    @Override
    public VAO getSkinModel()
    {
        return skinModel;
    }

    /**
     * Returns the {@link ResourceLocation} that represents the texture of this skin on disk.
     *
     * @return The texture of the skin on disk.
     */
    @Override
    public ResourceLocation getTexture()
    {
        return texture;
    }

    @Override
    public String toString()
    {
        return "AnimatrixSkin{" +
                 "skinModel=" + skinModel +
                 ", texture=" + texture +
                 '}';
    }
}
