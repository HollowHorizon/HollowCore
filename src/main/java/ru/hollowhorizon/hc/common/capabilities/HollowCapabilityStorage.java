package ru.hollowhorizon.hc.common.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class HollowCapabilityStorage implements Capability.IStorage<HollowCapability<?>> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<HollowCapability<?>> capability, HollowCapability<?> instance, Direction side) {

        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<HollowCapability<?>> capability, HollowCapability<?> instance, Direction side, INBT nbt) {
        if (nbt.getType() == CompoundNBT.TYPE) {
            instance.readNBT((CompoundNBT) nbt);
        }
    }
}
