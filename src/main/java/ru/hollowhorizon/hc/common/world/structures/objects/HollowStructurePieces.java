package ru.hollowhorizon.hc.common.world.structures.objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import ru.hollowhorizon.hc.common.registry.ModStructurePieces;

import java.util.List;
import java.util.Random;

public class HollowStructurePieces {

    public static void addPieces(TemplateManager manager, BlockPos pos, Rotation rot, List<StructurePiece> components, Random rand, ResourceLocation structure) {
        HollowPieces t = new HollowPieces(manager, structure, pos, 0, rot);
        components.add(t);
        t.addChildren(t, components, rand);
    }

    public static class HollowPieces extends TemplateStructurePiece {
        private final ResourceLocation template_name;
        private final Rotation rot;

        public HollowPieces(TemplateManager manager, ResourceLocation location, BlockPos pos, int offset, Rotation rotation) {
            super(ModStructurePieces.HollowStructurePiece, 0);
            this.template_name = location;
            this.rot = rotation;
            this.templatePosition = pos.above(offset);
            this.setup(manager);
        }

        public HollowPieces(TemplateManager templateManager, CompoundNBT compoundNBT) {
            super(ModStructurePieces.HollowStructurePiece, compoundNBT);
            this.template_name = new ResourceLocation(compoundNBT.getString("Template"));
            this.rot = Rotation.valueOf(compoundNBT.getString("Rot"));
            this.setup(templateManager);
        }

        private void setup(TemplateManager manager) {
            Template template = manager.getOrCreate(this.template_name);
            PlacementSettings placementsettings = new PlacementSettings()
                    .setRotation(this.rot)
                    .setMirror(Mirror.NONE)
                    .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            this.setup(template, this.templatePosition, placementsettings);
        }

        @Override
        protected void addAdditionalSaveData(CompoundNBT tagCompound) {
            super.addAdditionalSaveData(tagCompound);
            tagCompound.putString("Template", this.template_name.toString());
            tagCompound.putString("Rot", this.rot.toString());
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand, MutableBoundingBox sbb) {
        }

        @Override
        public boolean postProcess(ISeedReader p_230383_1_, StructureManager p_230383_2_, ChunkGenerator p_230383_3_, Random p_230383_4_, MutableBoundingBox p_230383_5_, ChunkPos p_230383_6_, BlockPos p_230383_7_) {
            return super.postProcess(p_230383_1_, p_230383_2_, p_230383_3_, p_230383_4_, p_230383_5_, p_230383_6_, p_230383_7_);
        }
    }

}
