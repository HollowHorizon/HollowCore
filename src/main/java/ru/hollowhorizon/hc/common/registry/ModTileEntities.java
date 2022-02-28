package ru.hollowhorizon.hc.common.registry;

import net.minecraft.tileentity.TileEntityType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile;

public class ModTileEntities {
    @HollowRegister(model = "hc:models/animstion1.fbx")
    public static final TileEntityType<SaveObeliskTile> SAVE_OBELISK_TILE = TileEntityType.Builder.of(
            SaveObeliskTile::new, ModBlocks.SAVE_OBELISK_BLOCK
    ).build(null);
}
