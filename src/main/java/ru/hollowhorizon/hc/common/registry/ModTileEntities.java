package ru.hollowhorizon.hc.common.registry;

import net.minecraft.tileentity.TileEntityType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.blocks.multi.block.MultiModuleBlock;
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiCoreBlockEntity;
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiModuleBlockEntity;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;

public class ModTileEntities {
    @HollowRegister()
    public static final TileEntityType<SaveObeliskTile> SAVE_OBELISK_TILE = TileEntityType.Builder.of(
            SaveObeliskTile::new, ModBlocks.SAVE_OBELISK_BLOCK
    ).build(null);

    @HollowRegister
    public static final TileEntityType<MultiModuleBlockEntity> MULTI_MODULE_BLOCK_ENTITY = TileEntityType.Builder.of(
            MultiModuleBlockEntity::new, ModBlocks.MULTI_MODULE_BLOCK
    ).build(null);

    @HollowRegister
    public static final TileEntityType<MultiCoreBlockEntity> MULTI_CORE_BLOCK_ENTITY = TileEntityType.Builder.of(
            MultiCoreBlockEntity::new, ModBlocks.MULTI_CORE_BLOCK
    ).build(null);
}
