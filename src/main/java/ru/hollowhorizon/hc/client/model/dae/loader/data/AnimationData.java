package ru.hollowhorizon.hc.client.model.dae.loader.data;

/**
 * Contains the extracted data for an animation, which includes the length of
 * the entire animation and the data for all the keyframes of the animation.
 * 
 * @author Karl
 *
 */
public class AnimationData {

    private final float          lengthSeconds;
    private final KeyFrameData[] keyFrames;

	public AnimationData(final float lengthSeconds, final KeyFrameData[] keyFrames) {
		this.lengthSeconds = lengthSeconds;
		this.keyFrames = keyFrames;
	}

    public float getLengthSeconds()
    {
        return lengthSeconds;
    }

    public KeyFrameData[] getKeyFrames()
    {
        return keyFrames;
    }
}
