package ru.hollowhorizon.hc.common.registry;

import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.blocks.HollowBlock;
import ru.hollowhorizon.hc.common.objects.blocks.SaveObelisk;

public class ModBlocks {
    @HollowRegister(auto_model = true)
    public static final HollowBlock SAVE_OBELISK_BLOCK = new SaveObelisk();
}
