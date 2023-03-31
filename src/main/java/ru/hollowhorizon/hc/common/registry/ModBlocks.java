package ru.hollowhorizon.hc.common.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.blocks.HollowBlock;
import ru.hollowhorizon.hc.common.objects.blocks.SaveObelisk;
import ru.hollowhorizon.hc.common.objects.blocks.multi.block.MultiCoreBlock;
import ru.hollowhorizon.hc.common.objects.blocks.multi.block.MultiModuleBlock;

public class ModBlocks {
    private ModBlocks() {
    }

    @HollowRegister(auto_model = true)
    public static final HollowBlock SAVE_OBELISK_BLOCK = new SaveObelisk();

    @HollowRegister
    public static final MultiModuleBlock MULTI_MODULE_BLOCK = new MultiModuleBlock(AbstractBlock.Properties.of(Material.METAL).harvestLevel(1).harvestTool(ToolType.PICKAXE).strength(0.3f).noOcclusion());

    @HollowRegister
    public static final MultiCoreBlock MULTI_CORE_BLOCK = new MultiCoreBlock();
}
