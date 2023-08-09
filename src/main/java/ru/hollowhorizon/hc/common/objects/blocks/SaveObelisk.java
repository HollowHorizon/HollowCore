package ru.hollowhorizon.hc.common.objects.blocks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class SaveObelisk extends HollowBlock {

    public SaveObelisk() {
        super(BlockBehaviour.Properties.of(Material.METAL).noOcclusion());

    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, Player player, @Nonnull InteractionHand p_225533_5_, @Nonnull BlockHitResult p_225533_6_) {
        return super.use(state, world, pos, player, p_225533_5_, p_225533_6_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SaveObeliskTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> type) {
        return type == ModTileEntities.SAVE_OBELISK_TILE ? SaveObeliskTile::tick : null;
    }

    @Override
    public Item.Properties getProperties() {
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }
}
