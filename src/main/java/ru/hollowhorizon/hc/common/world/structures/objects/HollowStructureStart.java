package ru.hollowhorizon.hc.common.world.structures.objects;

import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.WorldHelper;
import ru.hollowhorizon.hc.common.world.structures.config.BlockPosConfig;

import java.util.Random;

public class HollowStructureStart<T extends IFeatureConfig> extends StructureStart<T> {
    private final ResourceLocation structurePath;

    public HollowStructureStart(HollowStructure<T> structure, int chunkX, int chunkY, MutableBoundingBox boundingBox, int references, long seed) {
        super(structure, chunkX, chunkY, boundingBox, references, seed);
        this.structurePath = structure.getTemplatePoolPath();
    }

    @Override
    public HollowStructure<T> getFeature() {
        return (HollowStructure<T>)super.getFeature();
    }

    protected int getStructurePieceDepth() {
        return 10;
    }

    protected Random getRandom() {
        return this.random;
    }

    protected boolean shouldAvoidRotating() {
        return false;
    }

    protected boolean shouldGenerateOnWorldSurface() {
        return true;
    }

    protected boolean checkAndAdjustGeneration(ChunkGenerator chunkGenerator, BlockPos.Mutable chunkCenter, Biome biome, T config) {
        return chunkGenerator.getFirstFreeHeight(chunkCenter.getX(), chunkCenter.getZ(), Heightmap.Type.WORLD_SURFACE_WG) > 0;
    }

    public void generatePieces(DynamicRegistries dynamicRegistries, ChunkGenerator generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biome, T config) {
        int x = (chunkX << 4) + 7;
        int z = (chunkZ << 4) + 7;
        int sl = generator.getSeaLevel();
        int y = sl + this.random.nextInt(generator.getGenDepth() - 2 - sl);
        BlockPos.Mutable blockpos = new BlockPos.Mutable(x, y, z);

        if(checkAndAdjustGeneration(generator, blockpos, biome, config)) {
            generateStructurePieces(dynamicRegistries, getStructurePieceDepth(), generator, templateManagerIn, blockpos, getRandom(), false, shouldGenerateOnWorldSurface(), config);
        }
    }

    protected void generateStructurePieces(DynamicRegistries registries, int maxDepth, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos chunkCenter, Random rand, boolean bool1, boolean generateOnSurface, T config) {
        Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];

        HollowStructurePieces.addPieces(templateManager, chunkCenter, rotation, this.pieces, this.random, this.structurePath);
        this.calculateBoundingBox();

        doPostPlacementOperations(maxDepth, chunkGenerator, chunkCenter, rand, config);

    }

    protected void doPostPlacementOperations(int maxDepth, ChunkGenerator chunkGenerator, BlockPos originPos, Random rand, T config) {}
}
