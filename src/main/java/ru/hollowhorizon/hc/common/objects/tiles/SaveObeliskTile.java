package ru.hollowhorizon.hc.common.objects.tiles;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import ru.hollowhorizon.hc.api.utils.IAnimated;
import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

public class SaveObeliskTile extends HollowTileEntity implements ITickableTileEntity, IAnimated {
    private boolean isAnimating = false;
    private boolean isActivated = false;

    public SaveObeliskTile() {
        super(ModTileEntities.SAVE_OBELISK_TILE);
    }

    @Override
    public void tick() {
        if (this.level != null) {

            if (!this.level.isClientSide) {
                ServerWorld world = (ServerWorld) this.level;

                for (ServerPlayerEntity player : world.players()) {
                    boolean isAdventureMode = player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
                    if (player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) > 64 * 64) {
                        if (!isAdventureMode) {
                            player.setGameMode(GameType.ADVENTURE);
                        }
                    } else {
                        if (isAdventureMode) {
                            player.setGameMode(GameType.SURVIVAL);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onAnimationUpdate(HollowAnimationManager manager) {
        if(isActivated) {
            if (!isAnimating) {
                manager.addAnimation("Scene", true);
                isAnimating = true;
            }
        }
    }

    public void activate(boolean activated) {
        isActivated = activated;
    }

    @Override
    public void saveNBT(CompoundNBT nbt) {
        nbt.putBoolean("is_activated", isActivated);
    }

    @Override
    public void loadNBT(CompoundNBT nbt) {
        if(nbt.contains("is_activated")) isActivated = nbt.getBoolean("is_activated");
    }

    public boolean isActivated() {
        return isActivated;
    }
}
