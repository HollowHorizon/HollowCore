package ru.hollowhorizon.hc.common.objects.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import ru.hollowhorizon.hc.api.utils.IAnimated;
import ru.hollowhorizon.hc.common.registry.ModEntities;
import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;

public class TestEntity extends MonsterEntity implements IAnimated {
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
    public void onAnimationUpdate(HollowAnimationManager manager) {
        if(this.hasEffect(Effects.GLOWING)) {
            manager.addAnimation("save_obelisk_activation", false);
        } else if (this.hasEffect(Effects.BLINDNESS)) {
            manager.addAnimation("save_obelisk_tick", false);
        }
    }
}
