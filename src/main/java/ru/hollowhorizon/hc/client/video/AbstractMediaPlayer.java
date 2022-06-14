package ru.hollowhorizon.hc.client.video;

import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public abstract class AbstractMediaPlayer {
    public abstract EmbeddedMediaPlayer api();
    public abstract void markToRemove();
    public abstract void cleanup();
    public boolean providesAPI() {
        return false;
    }
}
