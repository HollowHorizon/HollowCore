package ru.hollowhorizon.hc.client.video;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

public class VideoManager {
    public static final MediaPlayerFactory FACTORY = new MediaPlayerFactory("--no-metadata-network-access", "--file-logging", "--logfile", "logs/vlc.log", "--logmode", "text", "--verbose", "2", "--no-quiet");
}
