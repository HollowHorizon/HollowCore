package ru.hollowhorizon.hc.common.objects.tiles;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import ru.hollowhorizon.hc.client.hollow_config.HollowCoreConfig;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

public class SaveObeliskTile extends TileEntity implements ITickableTileEntity {
    public SaveObeliskTile() {
        super(ModTileEntities.SAVE_OBELISK_TILE);
    }

    @Override
    public void tick() {
        if (this.level != null) {
            if (!HollowCoreConfig.is_editing_mode.getValue()) {
                return;
            }

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
}
