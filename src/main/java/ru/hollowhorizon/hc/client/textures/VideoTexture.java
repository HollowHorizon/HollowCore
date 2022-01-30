package ru.hollowhorizon.hc.client.textures;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL42;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoTexture extends SimpleTexture {
    private final VideoOptions options;
    private FFmpegFrameGrabber frameGrabber;
    private Frame lastFrame;
    private int glTextureId;
    private int glPixelBufferId;
    private ByteBuffer pixelData;
    private int imageHeight;
    private int imageWidth;

    public VideoTexture(ResourceLocation location, VideoOptions options) {
        super(location);
        this.options = options;
    }

    public int getLength() {
        if(this.frameGrabber!=null) {
            return this.frameGrabber.getLengthInVideoFrames();
        } else {
            return -1;
        }
    }

    public void initPlayer() {
        try {
            this.frameGrabber = new FFmpegFrameGrabber(Minecraft.getInstance().getResourceManager().getResource(location).getInputStream());
            this.frameGrabber.start();

            if (this.frameGrabber.hasVideo()) {
                this.imageWidth = this.frameGrabber.getImageWidth();
                this.imageHeight = this.frameGrabber.getImageHeight();
                this.glTextureId = GL42.glGenTextures();
                GL42.glBindTexture(3553, this.glTextureId);
                GL42.glTexParameteri(3553, 10240, 9729);
                GL42.glTexParameteri(3553, 10241, 9729);
                GL42.glTexParameteri(3553, 33084, 0);
                GL42.glTexParameteri(3553, 33085, 0);
                GL42.glTexParameteri(3553, 10242, 33069);
                GL42.glTexParameteri(3553, 10243, 33069);
                GL42.glTexImage2D(3553, 0, 32849, this.imageWidth, this.imageHeight, 0, 32992, 5121, (ByteBuffer) null);

                this.glPixelBufferId = GL42.glGenBuffers();
                GL42.glBindBuffer(35052, this.glPixelBufferId);
                GL42.glBufferData(35052, (this.imageWidth * this.imageHeight * 3L), 35040);
                GL42.glBindBuffer(35052, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bind() {
        try {
            Frame capturedFrame = this.frameGrabber.grab();

            if (capturedFrame == null) {
                if (options == VideoOptions.LOOP) {
                    this.frameGrabber.stop();
                    this.frameGrabber = null;
                    initPlayer();
                    bind();
                } else if (options == VideoOptions.END_AT_LAST_FRAME) {
                    this.updateTexture((ByteBuffer) lastFrame.image[0]);
                } else if (options == VideoOptions.END_AND_CLOSE) {
                    close();
                }
            } else {
                this.updateTexture((ByteBuffer) capturedFrame.image[0]);
                this.lastFrame = capturedFrame;
            }
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (this.frameGrabber != null) {
            try {
                this.frameGrabber.stop();
            } catch (FFmpegFrameGrabber.Exception e) {
                e.printStackTrace();
            }
            RenderSystem.deleteTexture(this.glTextureId);
            GL42.glDeleteBuffers(this.glPixelBufferId);
            this.frameGrabber = null;
        }
    }

    public void updateTexture(ByteBuffer image) {
        GL42.glBindTexture(3553, this.glTextureId);
        GL42.glBindBuffer(35052, this.glPixelBufferId);
        this.pixelData = GL15C.glMapBuffer(35052, 35001, image.limit(), this.pixelData);
        GL42.glPixelStorei(3314, 0);
        GL42.glPixelStorei(32878, 0);
        GL42.glPixelStorei(3315, 0);
        GL42.glPixelStorei(3316, 0);
        GL42.glPixelStorei(32877, 0);
        GL42.glPixelStorei(3317, 1);
        this.pixelData.put(image);
        GL42.glUnmapBuffer(35052);
        GL42.glTexSubImage2D(3553, 0, 0, 0, this.imageWidth, this.imageHeight, 32992, 5121, 0L);
        GL42.glBindBuffer(35052, 0);
        GL42.glBindTexture(3553, 0);
        RenderSystem.bindTexture(this.glTextureId);
    }

    public enum VideoOptions {
        END_AND_CLOSE,
        LOOP,
        END_AT_LAST_FRAME
    }
}
