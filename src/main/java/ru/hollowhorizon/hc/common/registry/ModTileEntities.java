package ru.hollowhorizon.hc.common.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;

public class ModTileEntities {
    @HollowRegister()
    public static final BlockEntityType<SaveObeliskTile> SAVE_OBELISK_TILE = BlockEntityType.Builder.of(
            SaveObeliskTile::new, ModBlocks.SAVE_OBELISK_BLOCK
    ).build(null);
}
