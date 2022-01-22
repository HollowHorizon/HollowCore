package ru.hollowhorizon.hc.common.world.structures.objects;

import net.minecraft.block.BlockState;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.world.structures.config.StructureNameConfig;

import java.util.Random;

public class HollowBastion extends StoryStructure {
    public HollowBastion(GenerationStage.Decoration decorationStage, String templatePoolPath) {
        super(decorationStage, templatePoolPath);
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeProvider p_230363_2_, long p_230363_3_, SharedSeedRandom p_230363_5_, int chunkX, int chunkZ, Biome p_230363_8_, ChunkPos p_230363_9_, StructureNameConfig config) {
        BlockPos centerOfChunk = new BlockPos((chunkX << 4) + 7, 0, (chunkZ << 4) + 7);
        int landHeight = chunkGenerator.getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        IBlockReader columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ());
        BlockState topBlock = columnOfBlocks.getBlockState(centerOfChunk.above(landHeight).above());

        return topBlock.getFluidState().isEmpty();
    }

    @Override
    protected HollowStartFactory<StructureNameConfig> getStructureStart() {
        return (structure, chunkX, chunkZ, boundingBox, references, seed) -> new StoryStructureStart<StructureNameConfig>(structure, chunkX, chunkZ, boundingBox, references, seed) {
            @Override
            protected boolean shouldGenerateOnWorldSurface() {
                return false;
            }

            @Override
            protected int getStructurePieceDepth() {
                return 6;
            }

            @Override
            protected boolean checkAndAdjustGeneration(ChunkGenerator chunkGenerator, BlockPos.Mutable chunkCenter, Biome biome, StructureNameConfig config) {
                chunkCenter.setY(chunkCenter.getY() - 22);
                return super.checkAndAdjustGeneration(chunkGenerator, chunkCenter, biome, config);
            }

            @Override
            protected void doPostPlacementOperations(int maxDepth, ChunkGenerator chunkGenerator, BlockPos originPos, Random rand, StructureNameConfig config) {
                super.doPostPlacementOperations(maxDepth, chunkGenerator, originPos, rand, config);
                ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach((playerEntity -> playerEntity.sendMessage(new StringTextComponent("structure here: " + this.pieces.get(0).getBoundingBox().x0 + " " + this.pieces.get(0).getBoundingBox().y0 + " " + this.pieces.get(0).getBoundingBox().z0), playerEntity.getUUID())));
            }

        };
    }
}
