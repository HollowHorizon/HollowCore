package ru.hollowhorizon.hc.common.events;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;

public class UniversalContainerEvent extends Event {
    private final String containerName;
    private final CompoundNBT nbt;

    public UniversalContainerEvent(String containerName, CompoundNBT nbt) {
        this.containerName = containerName;
        this.nbt = nbt;
    }

    public CompoundNBT getNBT() {
        return nbt;
    }

    public String getContainerName() {
        return containerName;
    }
}
