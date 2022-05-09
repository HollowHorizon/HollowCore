package ru.hollowhorizon.hc.client.model.dae.model.animator;

import net.minecraft.entity.Entity;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;

public class AnimatrixLivingAnimator extends AnimatrixAnimator {

    public AnimatrixLivingAnimator(final IModel model) {
        super(model);
    }

    public void onUpdateRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {

    }

}
