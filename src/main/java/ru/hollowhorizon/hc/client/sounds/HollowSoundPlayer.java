package ru.hollowhorizon.hc.client.sounds;

import javax.sound.sampled.*;
import java.io.IOException;

public class HollowSoundPlayer {
    private Clip clip;

    public void play() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(HollowSoundPlayer.class.getResource("NameOfFile.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        clip.stop();
    }
}
