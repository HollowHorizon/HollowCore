package ru.hollowhorizon.hc.client.video;

import ru.hollowhorizon.hc.HollowCore;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;

public class DefaultBufferFormatCallback extends BufferFormatCallbackAdapter {
    public final MediaPlayerBase mediaPlayerBase;

    public DefaultBufferFormatCallback(MediaPlayerBase mediaPlayer) {
        mediaPlayerBase = mediaPlayer;
    }

    @Override
    public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
        HollowCore.LOGGER.info("Dimensions of player {}: {} | {}", mediaPlayerBase.dynamicResourceLocation, sourceWidth, sourceHeight);
        mediaPlayerBase.callback.setBuffer(sourceWidth, sourceHeight);
        return new RGBABufferFormat(sourceWidth, sourceHeight);
    }

    /**
     * Implementation of a buffer format for RGB.
     * <p>
     * RGBA is a 32-bit RGBA format in a single plane.
     */
    public static class RGBABufferFormat extends BufferFormat {

        /**
         * Creates an RGBA buffer format with the given width and height.
         *
         * @param width width of the buffer
         * @param height height of the buffer
         */
        public RGBABufferFormat(int width, int height) {
            super("RGBA", width, height, new int[] {width * 4}, new int[] {height});
        }

    }
}
