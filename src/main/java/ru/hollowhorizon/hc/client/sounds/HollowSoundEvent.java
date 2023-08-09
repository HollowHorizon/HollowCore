package ru.hollowhorizon.hc.client.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class HollowSoundEvent extends SoundEvent {
    public HollowSoundEvent(String modId, String path) {
        super(new ResourceLocation(modId, path));
    }
}
