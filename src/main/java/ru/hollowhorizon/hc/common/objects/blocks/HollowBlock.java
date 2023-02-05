package ru.hollowhorizon.hc.common.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public abstract class HollowBlock extends Block implements IBlockProperties {
    public HollowBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }
}
