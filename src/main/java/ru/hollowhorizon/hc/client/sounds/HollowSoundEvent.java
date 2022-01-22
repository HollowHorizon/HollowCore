package ru.hollowhorizon.hc.client.sounds;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class HollowSoundEvent extends SoundEvent {
    public HollowSoundEvent(String modId, String path) {
        super(new ResourceLocation(modId, path));
    }
}
