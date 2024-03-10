package ru.hollowhorizon.hc.common.objects.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;
import ru.hollowhorizon.hc.common.registry.ModTileEntities;

public class SaveObeliskBlock extends HollowBlock implements EntityBlock {

    public SaveObeliskBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL).noOcclusion());
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState otherState, @NotNull Direction direction) {
        return true;
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SaveObeliskTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == ModTileEntities.INSTANCE.getSAVE_OBELISK_TILE().get() ? SaveObeliskTile::tick : null;
    }

    @Override
    public Item.Properties getProperties() {
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }
}
