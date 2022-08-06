package ru.hollowhorizon.hc.common.world.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.world.structures.StoryStructureData;

import java.util.ArrayList;

public class HollowWorldData extends WorldSavedData {
    public static HollowWorldData INSTANCE;
    public ArrayList<StoryStructureData> STRUCTURE_DATA_LIST = new ArrayList<>();

    public HollowWorldData() {
        super("hollow_world_data");
    }

    @Override
    public void load(CompoundNBT nbt) {
        STRUCTURE_DATA_LIST = NBTUtils.loadList(nbt, "story_structures", StoryStructureData.SERIALIZER);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        NBTUtils.saveList(nbt, "story_structures", STRUCTURE_DATA_LIST, StoryStructureData.SERIALIZER);
        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
