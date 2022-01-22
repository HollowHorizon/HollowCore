package ru.hollowhorizon.hc.common.events;

import net.minecraftforge.eventbus.api.Event;

public class OnDialogueSound extends Event {
    public String sound;
    public OnDialogueSound(String sound) {
        this.sound = sound;
    }
}
