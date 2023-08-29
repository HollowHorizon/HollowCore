package ru.hollowhorizon.hc.common.objects.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

public class SaveObeliskTile extends HollowTileEntity {
    private final boolean isAnimating = false;
    private boolean isActivated = false;

    public SaveObeliskTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.INSTANCE.getSAVE_OBELISK_TILE().get(), pos, state);
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T tile) {
        if (level != null) {

            if (!level.isClientSide) {
                ServerLevel world = (ServerLevel) level;

                for (ServerPlayer player : world.players()) {
                    boolean isAdventureMode = player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
                    if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64 * 64) {
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

    public void activate(boolean activated) {
        isActivated = activated;
    }

    @Override
    public void saveNBT(CompoundTag nbt) {
        nbt.putBoolean("is_activated", isActivated);
    }

    @Override
    public void loadNBT(CompoundTag nbt) {
        if (nbt.contains("is_activated")) isActivated = nbt.getBoolean("is_activated");
    }

    public boolean isActivated() {
        return isActivated;
    }


}
