package ru.hollowhorizon.hc.client.model.dae.model;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animator.IAnimator;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.ISkeleton;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skin.ISkin;
import ru.hollowhorizon.hc.client.model.dae.model.animator.AnimatrixAnimator;

/**
 * Represents a single model in Animatrix.
 */
@OnlyIn(Dist.CLIENT)
public class AnimatrixModel implements IModel
{
    private final ISkeleton skeleton;
    private final ISkin skin;
    private final IAnimator animator;

    public AnimatrixModel(final ISkeleton skeleton, final ISkin skin) {
        this.skeleton = skeleton;
        this.skin = skin;
        this.animator = new AnimatrixAnimator(this);
    }

    /**
     * The skeleton of the model.
     *
     * @return The skeleton.
     */
    @Override
    public ISkeleton getSkeleton()
    {
        return skeleton;
    }

    /**
     * The skin of the model.
     *
     * @return The skin.
     */
    @Override
    public ISkin getSkin()
    {
        return skin;
    }

    /**
     * The animator for the model.
     *
     * @return The animator.
     */
    @Override
    public IAnimator getAnimator()
    {
        return animator;
    }
}
