package ru.hollowhorizon.hc.client.video;

import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;

public class MediaPlayerCallback extends RenderCallbackAdapter {
    private final MediaPlayerBase mediaPlayer;
    private int width;

    public MediaPlayerCallback(int width, MediaPlayerBase mediaPlayer) {
        this.width = width;
        this.mediaPlayer = mediaPlayer;
    }

    public void setBuffer(int sourceWidth, int sourceHeight) {
        this.width = sourceWidth;
        setBuffer(new int[sourceWidth * sourceHeight]);
    }

    @Override
    protected void onDisplay(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, int[] buffer) {
        this.mediaPlayer.setIntBuffer(new IntegerBuffer2D(width, buffer));
    }
}
