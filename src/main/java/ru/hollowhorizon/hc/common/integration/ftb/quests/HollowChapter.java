package ru.hollowhorizon.hc.common.integration.ftb.quests;

import net.minecraft.nbt.CompoundNBT;

public interface HollowChapter {
    CompoundNBT getExtra();

    void setExtra(CompoundNBT extra);
}
