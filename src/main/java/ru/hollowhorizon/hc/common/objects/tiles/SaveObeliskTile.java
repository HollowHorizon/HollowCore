/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.objects.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability;
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

public class SaveObeliskTile extends HollowTileEntity implements IAnimated {
    private boolean isActivated = false;

    public SaveObeliskTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.INSTANCE.getSAVE_OBELISK_TILE().get(), pos, state);
        AnimatedEntityCapability capability = ForgeKotlinKt.get(this, AnimatedEntityCapability.class);
        capability.setModel("hc:models/entity/boom_box.gltf");
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState ignoredState, T ignoredTile) {
        if (level != null && !level.isClientSide) {
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
