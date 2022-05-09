package ru.hollowhorizon.hc.common.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HollowPlayerProvider implements ICapabilitySerializable<INBT> {
    public static final byte COMPOUND_NBT_ID = new CompoundNBT().getId();
    private final static String HOLLOW_STORY_NBT = "hollow_story";
    private final HollowStoryCapability storyCapability = new HollowStoryCapability();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (ModCapabilities.STORY_CAPABILITY == capability) {
            return (LazyOptional<T>)LazyOptional.of(()-> storyCapability);
        }
        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        INBT riskNBT = ModCapabilities.STORY_CAPABILITY.writeNBT(storyCapability, null);
        nbt.put(HOLLOW_STORY_NBT, riskNBT);
        return nbt;
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            HollowCore.LOGGER.warn("Unexpected NBT type: "+nbt);
            return;
        }
        CompoundNBT compoundNBT = (CompoundNBT)nbt;

        INBT riskNBT = compoundNBT.get(HOLLOW_STORY_NBT);
        ModCapabilities.STORY_CAPABILITY.readNBT(storyCapability, null, riskNBT);
    }
}
