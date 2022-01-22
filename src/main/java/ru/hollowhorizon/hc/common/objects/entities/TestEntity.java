package ru.hollowhorizon.hc.common.objects.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import ru.hollowhorizon.hc.api.utils.IAnimatedEntity;
import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;
import ru.hollowhorizon.hc.client.render.mmd.MMDModelManager;
import ru.hollowhorizon.hc.common.registry.ModEntities;

public class TestEntity extends MonsterEntity implements IAnimatedEntity {
    private final HollowAnimationManager manager = new HollowAnimationManager();
    private boolean isCustom;

    public TestEntity(World level) {
        super(ModEntities.testEntity, level);
    }

    public TestEntity(EntityType<? extends MonsterEntity> p_i48553_1_, World p_i48553_2_) {
        super(p_i48553_1_, p_i48553_2_);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(0, new RandomWalkingGoal(this, 0.3D));
    }

    @Override
    public void processAnimation() {
        if(isCustom) return;
        if (this.isVehicle()) {
            manager.setAnimation("ridden");
        } else if (this.isInWater()) {
            manager.setAnimation("swim");
        } else if (this.getX() - this.xo != 0.0f || this.getZ() - this.zo != 0.0f) {
            manager.setAnimation("walk");
        } else {
            manager.setAnimation("idle");
        }
    }

    @Override
    public void setAnimation(String animation) {
        manager.setAnimation(animation);
    }

    @Override
    public void setCustomAnimation(String animation) {
        isCustom = true;
        this.setAnimation(animation);
    }

    @Override
    public void endCustomAnimation() {
        isCustom = false;
    }

    @Override
    public boolean isCustomAnimation() {
        return isCustom;
    }

    @Override
    public HollowAnimationManager getManager() {
        return manager;
    }
}
