package ru.hollowhorizon.hc.common.objects.blocks;


import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public abstract class HollowBlock extends HorizontalDirectionalBlock implements IBlockProperties {
    public HollowBlock(Properties properties) {
        super(properties);
    }
}
