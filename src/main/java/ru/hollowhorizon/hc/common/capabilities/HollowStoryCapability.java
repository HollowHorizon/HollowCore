package ru.hollowhorizon.hc.common.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowStoryCapability extends HollowCapability<HollowStoryCapability> {
    private final Map<String, CompoundNBT> storyData = new HashMap<>();

    public HollowStoryCapability() {
        super(new ResourceLocation(MODID, "hollow_story_capability"));
    }

    public void addStory(String storyName, CompoundNBT data) {
        HollowCore.LOGGER.info("add story");
        storyData.put(storyName, data);
    }

    public void removeStory(String storyName) {
        storyData.remove(storyName);
    }

    public CompoundNBT getStory(String name) {
        return storyData.get(name);
    }

    @Override
    public Capability<HollowStoryCapability> getCapability() {
        return ModCapabilities.STORY_CAPABILITY;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("array_size", storyData.size());
        int i = 0;
        for (CompoundNBT compound : storyData.values()) {
            nbt.put("data_" + i, compound);
            i++;
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        storyData.clear();

        int size = nbt.getInt("array_size");

        for (int i = 0; i < size; i++) {
            CompoundNBT storyData = nbt.getCompound("data_" + i);
            this.storyData.put(storyData.getString("story_name"), storyData);
        }
    }

    public boolean hasStory(String storyName) {
        return storyData.containsKey(storyName);
    }

    public Set<String> getAll() {
        return storyData.keySet();
    }
}
