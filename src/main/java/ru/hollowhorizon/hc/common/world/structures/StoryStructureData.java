package ru.hollowhorizon.hc.common.world.structures;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;

public class StoryStructureData {
    public static final HollowNBTSerializer<StoryStructureData> SERIALIZER = new HollowNBTSerializer<StoryStructureData>("story_structure_data") {
        @Override
        public StoryStructureData fromNBT(CompoundNBT nbt) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            String name = nbt.getString("name");
            return new StoryStructureData(new ResourceLocation(name), new BlockPos(x, y, z));
        }

        @Override
        public CompoundNBT toNBT(StoryStructureData value) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("name", value.location.toString());
            nbt.putInt("x", value.structurePos.getX());
            nbt.putInt("y", value.structurePos.getY());
            nbt.putInt("z", value.structurePos.getZ());
            return nbt;
        }
    };
    private final BlockPos structurePos;
    private final ResourceLocation location;

    public StoryStructureData(ResourceLocation location, BlockPos structurePos) {
        this.location = location;
        this.structurePos = structurePos;
    }

    public ResourceLocation getStructureName() {
        return location;
    }

    public BlockPos getStructurePos() {
        return structurePos;
    }
}
