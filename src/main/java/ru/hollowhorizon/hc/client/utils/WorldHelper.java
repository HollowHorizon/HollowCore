package ru.hollowhorizon.hc.client.utils;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.FolderName;

public class WorldHelper {
    private WorldHelper() {
    }

    public static FolderName create(String id) {
        return new FolderName(id);
    }

    public static int getHighestBlockY(World world, int x, int z) {
        return getHighestBlock(world, x, z).getY();
    }

    public static BlockPos getHighestBlock(World world, int x, int z) {
        int y_f = 254;
        for(int y = 254; isReplaceableByStructures(world.getBlockState(new BlockPos(x, y, z))); y--) {
            y_f = y;
        }
        return new BlockPos(x, y_f, z);
    }

    private static boolean isReplaceableByStructures(BlockState blockState) {
        return blockState.getBlock().is(Blocks.AIR) || blockState.getMaterial().isLiquid() || blockState.getMaterial().isReplaceable();
    }

    public static class FreePlaceInfo {
        public final int y;
        public final int length;

        public FreePlaceInfo(int y, int length) {
            this.y = y;
            this.length = length;
        }
    }
}
