package ru.hollowhorizon.hc.common.animations;

import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.HollowCore;

import java.util.ArrayList;
import java.util.List;

public abstract class CutsceneHandler implements HollowCutscene {
    protected ServerPlayerEntity player;
    private BlockPos startPos;
    private final List<AnimationHandler.AnimationAction> actions = new ArrayList<>();

    @Override
    public void start(ServerPlayerEntity player) {
        if(startPos != null) {
            CompoundNBT data = new CompoundNBT();
            data.putString("cutscene_name", getName());
            data.putInt("x", startPos.getX());
            data.putInt("y", startPos.getY());
            data.putInt("z", startPos.getZ());
            HollowCore.LOGGER.info("coordinates: " + startPos.getX() + " " + startPos.getY() + " " + startPos.getZ());
            player.getPersistentData().put("current_cutscene", data);

            player.teleportTo(startPos.getX(), startPos.getY(), startPos.getZ());

            this.player = player;

            this.player.setGameMode(GameType.SPECTATOR);

            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void moveTo(BlockPos startPos, BlockPos endPos, int timeTicks) {
        actions.add(new AnimationHandler.AnimationAction() {
            private int ticks = 0;

            @Override
            public void tick() {
                Vector3d vec = new Vector3d(
                        startPos.getX() + ((endPos.getX() - startPos.getX()) / (timeTicks + 0F) * ticks),
                        startPos.getY() + ((endPos.getY() - startPos.getY()) / (timeTicks + 0F) * ticks),
                        startPos.getZ() + ((endPos.getZ() - startPos.getZ()) / (timeTicks + 0F) * ticks)
                );
                float playerYaw = (float) Math.toDegrees(MathHelper.atan2(vec.z, vec.x));

                player.teleportTo(
                        player.getLevel(),
                        vec.x,
                        vec.y,
                        vec.z,
                        playerYaw, 0
                );

                ticks++;
            }

            @Override
            public boolean end() {
                return ticks > timeTicks;
            }
        });
    }

    public <M extends MobEntity> void watchMob(M mobEntity, AnimationHandler.EndCheck isEnd) {
        actions.add(new AnimationHandler.AnimationAction() {
            @Override
            public void tick() {
                player.lookAt(EntityAnchorArgument.Type.EYES, mobEntity, EntityAnchorArgument.Type.EYES);
            }

            @Override
            public boolean end() {
                return isEnd.isEnd();
            }
        });
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (actions.size() > 0) {
            AnimationHandler.AnimationAction action = actions.get(0);

            action.tick();

            if (action.end()) {
                action.onEnd();
                actions.remove(0);
            }
        } else {
            this.stop();
        }
    }

    public void setStartPos(BlockPos pos) {
        this.startPos = pos;
    }

    @Override
    public void stop() {
        this.player.getPersistentData().remove("current_cutscene");

        this.player.setGameMode(GameType.SURVIVAL);

        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
