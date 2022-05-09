package ru.hollowhorizon.hc.common.world.structures.objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import ru.hollowhorizon.hc.common.world.storage.HollowWorldData;
import ru.hollowhorizon.hc.common.world.structures.StoryStructureData;
import ru.hollowhorizon.hc.common.world.structures.config.StructureNameConfig;

import java.util.Random;

public class StoryStructureStart<T extends StructureNameConfig> extends HollowStructureStart<T> {
    public StoryStructureStart(HollowStructure<T> structure, int chunkX, int chunkY, MutableBoundingBox boundingBox, int references, long seed) {
        super(structure, chunkX, chunkY, boundingBox, references, seed);
    }

    @Override
    protected boolean checkAndAdjustGeneration(ChunkGenerator chunkGenerator, BlockPos.Mutable chunkCenter, Biome biome, T config) {
        for (StoryStructureData data : HollowWorldData.INSTANCE.STRUCTURE_DATA_LIST) {
            if(config.structureName.equals(data.getStructureName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doPostPlacementOperations(int maxDepth, ChunkGenerator chunkGenerator, BlockPos originPos, Random rand, T config) {
        int x = this.pieces.get(0).getBoundingBox().x0;
        int y = this.pieces.get(0).getBoundingBox().y0;
        int z = this.pieces.get(0).getBoundingBox().z0;

        HollowWorldData.INSTANCE.STRUCTURE_DATA_LIST.add(new StoryStructureData(this.getFeature().getRegistryName(), new BlockPos(x,y,z)));
    }
}
