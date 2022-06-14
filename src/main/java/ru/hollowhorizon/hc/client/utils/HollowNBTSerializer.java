package ru.hollowhorizon.hc.client.utils;

import net.minecraft.nbt.CompoundNBT;

import java.io.IOException;

public abstract class HollowNBTSerializer<T> {
    public HollowNBTSerializer(String s) {
        NBTUtils.addSerializer(this, s);
    }

    public abstract T fromNBT(CompoundNBT nbt);

    public abstract CompoundNBT toNBT(T value);
}
