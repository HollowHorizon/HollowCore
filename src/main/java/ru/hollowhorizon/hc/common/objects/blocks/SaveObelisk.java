package ru.hollowhorizon.hc.common.objects.blocks;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SaveObelisk extends HollowBlock {

    public SaveObelisk() {
        super(Properties.of(Material.METAL).noOcclusion());

    }

    @Nonnull
    @Override
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand p_225533_5_, @Nonnull BlockRayTraceResult p_225533_6_) {
        return super.use(state, world, pos, player, p_225533_5_, p_225533_6_);
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderShape(@Nonnull BlockState p_149645_1_) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SaveObeliskTile();
    }

    @Override
    public Item.Properties getProperties() {
        return new Item.Properties().tab(ItemGroup.TAB_MISC);
    }
}
