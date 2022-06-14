package ru.hollowhorizon.hc.client.video.media;

import net.minecraftforge.registries.ForgeRegistryEntry;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableCallbackMedia;

public class MediaEntry extends ForgeRegistryEntry<MediaEntry> {
    private final NonSeekableCallbackMedia media;

    public MediaEntry(NonSeekableCallbackMedia media) {
        this.media = media;
    }

    public NonSeekableCallbackMedia getMedia() {
        return media;
    }
}
