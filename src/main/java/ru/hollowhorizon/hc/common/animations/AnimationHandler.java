package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import ru.hollowhorizon.hc.HollowCore;

import java.util.ArrayList;
import java.util.List;

public class AnimationHandler<T extends MobEntity> {
    public final T mobEntity;
    public final List<AnimationAction> actions = new ArrayList<>();
    private IEndable endHandler;

    public AnimationHandler(T mob) {
        this.mobEntity = mob;
    }

    public static <T extends MobEntity> AnimationHandler<T> create(ServerWorld level, BlockPos pos, EntityType<T> type) {
        AnimationHandler<T> handler = new AnimationHandler<>(type.create(level));
        handler.getMob().setPos(pos.getX(), pos.getY(), pos.getZ());
        level.addFreshEntity(handler.getMob());
        handler.getMob().finalizeSpawn(level, level.getCurrentDifficultyAt(pos), SpawnReason.EVENT, null, null);
        return handler;
    }

    public void start() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void end() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.mobEntity.remove();

        if (endHandler != null) {
            endHandler.onEnd();
        }
    }

    public void onEnd(IEndable onEnd) {
        endHandler = onEnd;
    }

    public T getMob() {
        return mobEntity;
    }

    public void setMobUnkillable() {
        mobEntity.setInvulnerable(true);
    }

    public void disableAI() {
        mobEntity.goalSelector.availableGoals.clear();
        mobEntity.targetSelector.availableGoals.clear();
    }

    public void addAction(AnimationAction action) {
        actions.add(action);
    }

    public void goToPos(double x, double y, double z) {
        actions.add(new AnimationAction() {
            @Override
            public void tick() {
                mobEntity.getNavigation().moveTo(x, y, z, 0.7F);
            }

            @Override
            public boolean end() {
                HollowCore.LOGGER.info(mobEntity.distanceToSqr(x, y, z));
                return MathHelper.sqrt(mobEntity.distanceToSqr(x, y, z)) < 5;
            }
        });
    }

    public void spin(BlockPos targetPos, int ticks, int height, int radius, float speed) {
        actions.add(new AnimationAction() {
            private float angleRadians = 0F;
            private int counter = 0;

            @Override
            public void tick() {

                this.angleRadians = (float) ((double) this.angleRadians + Math.toRadians(3.0D));

                float rad = 38.0F + 4.0F * MathHelper.cos(this.angleRadians / 4.0F);

                Vector3d vec = new Vector3d(rad * MathHelper.cos(this.angleRadians), 4.0F * MathHelper.cos(this.angleRadians * 2.0F), rad * MathHelper.sin(this.angleRadians));
                float dragonYaw = (float) Math.toDegrees(MathHelper.atan2(vec.z, vec.x));
                float dragonPitch = 0.0F;
                Vector3d dragonPos = new Vector3d((double) targetPos.getX() + vec.x, (double) targetPos.getY() + vec.y + 47.0D, (double) targetPos.getZ() + vec.z);
                mobEntity.absMoveTo(dragonPos.x(), dragonPos.y(), dragonPos.z(), dragonYaw, dragonPitch);

                counter++;
            }

            @Override
            public boolean end() {
                return this.counter >= ticks;
            }
        });
    }

    public void findPlayer(ServerPlayerEntity player, EndCheck check, SpeedChanger speedData) {
        actions.add(new AnimationAction() {
            private int counter = 0;
            private int canSeeCounter = 0;
            private boolean helpMobFind = false;
            private Vector3d lastTickPos = new Vector3d(0, 0, 0);

            @Override
            public void tick() {
                boolean canSeePlayer = mobEntity.canSee(player);
                float speed = speedData.getSpeed();

                if (canSeePlayer || helpMobFind) {
                    mobEntity.getNavigation().moveTo(player, speed);
                }

                if (!helpMobFind) {
                    if (player.isShiftKeyDown() || canSeeCounter > 500) {
                        if (lastTickPos.equals(player.position())) {
                            if (counter > 100) {
                                helpMobFind = true;
                                counter = 0;
                                canSeeCounter = 0;
                                return;
                            }
                            counter++;
                        }

                        lastTickPos = player.position();
                    }else {
                        canSeeCounter+=1;
                    }
                } else if (canSeePlayer) {
                    helpMobFind = false;
                }

            }

            @Override
            public boolean end() {
                return check.isEnd();
            }
        });
    }

    public void goToPlayer(ServerPlayerEntity player) {
        goToPlayer(player, 4);
    }

    public void goToPlayer(ServerPlayerEntity player, int endDistance) {
        actions.add(new AnimationAction() {
            @Override
            public void tick() {
                mobEntity.getNavigation().moveTo(player, 0.8F);
            }

            @Override
            public boolean end() {
                return mobEntity.distanceTo(player) < endDistance;
            }
        });
    }

    public void goToPlayerWhileNotSee(ServerPlayerEntity player) {
        actions.add(new AnimationAction() {
            @Override
            public void tick() {
                mobEntity.getNavigation().moveTo(player, 0.8F);
            }

            @Override
            public boolean end() {
                return player.canSee(mobEntity);
            }
        });
    }

    public void leave(ServerPlayerEntity player) {
        actions.add(new AnimationAction() {
            int ticks = 0;

            @Override
            public void tick() {
                mobEntity.getNavigation().moveTo(0, 0, 0, 0.7F);
                ticks++;
            }

            @Override
            public boolean end() {
                return !player.canSee(mobEntity) || ticks > 2000;
            }
        });
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (actions.size() > 0) {
            AnimationAction action = actions.get(0);

            action.tick();

            if (action.end()) {
                action.onEnd();
                actions.remove(0);
                HollowCore.LOGGER.info("действие завершено");
            }
        } else {
            this.end();
            return;
        }

        if (!mobEntity.isAlive()) {
            this.end();
        }
    }

    @SubscribeEvent
    public void onWorldClosed(FMLServerStoppingEvent event) {
        this.end();
    }

    public interface AnimationAction {
        void tick();

        boolean end();

        default void onEnd() {
        }
    }

    public interface IEndable {
        void onEnd();
    }

    public interface EndCheck {
        boolean isEnd();
    }

    public interface SpeedChanger {
        float getSpeed();
    }
}
