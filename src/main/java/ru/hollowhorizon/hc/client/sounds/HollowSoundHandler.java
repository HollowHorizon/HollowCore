package ru.hollowhorizon.hc.client.sounds;

import net.minecraft.util.SoundEvent;
import ru.hollowhorizon.hc.api.registy.HollowRegister;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowSoundHandler {
    @HollowRegister
    public static SoundEvent CHOICE_BUTTON = new HollowSoundEvent(MODID, "choice_button");
}
