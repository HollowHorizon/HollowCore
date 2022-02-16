//В разработке
//package ru.hollowhorizon.hc.client.screens;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.LoadingGui;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.WorldVertexBufferUploader;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.item.ItemEntity;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.vector.Matrix4f;
//import net.minecraft.world.World;
//import net.minecraftforge.event.entity.EntityJoinWorldEvent;
//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.bytedeco.javacv.Frame;
//import org.lwjgl.assimp.Assimp;
//import org.lwjgl.opengl.GL15C;
//import org.lwjgl.opengl.GL42;
//import ru.hollowhorizon.hc.HollowCore;
//
//import javax.annotation.Nonnull;
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.DataLine;
//import javax.sound.sampled.SourceDataLine;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.ShortBuffer;
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//
//public class VideoOverlay extends LoadingGui {
//    private final Runnable onFinish;
//    private final int QUEUE_SIZE = 200;
//    private final ArrayBlockingQueue<Frame> videoFrames;
//    private final ArrayBlockingQueue<Frame> audioFrames;
//    private int width;
//    private int height;
//    private FFmpegFrameGrabber frameGrabber;
//    private SourceDataLine soundLine;
//    private int glTextureId;
//    private int glPixelBufferId;
//    private ByteBuffer pixelData;
//    private boolean finished = false;
//    private boolean finishedGrabbing = false;
//    private int imageWidth;
//    private int imageHeight;
//    private float x2 = 2.0F;
//    private float y2 = 2.0F;
//    private float minU = 0.0F;
//    private float maxU = 2.0F;
//    private float minV = 0.0F;
//    private float maxV = 2.0F;
//    private long startTime;
//    private long oneVideoFrameTimeNanoSeconds;
//    private long oneAudioFrameTimeNanoSeconds;
//    private int frameTimestampToleranceMicroSeconds;
//    private int videoFrameCount;
//    private int audioFrameCount;
//
//    public VideoOverlay(InputStream stream, Runnable onFinish) {
//        this.videoFrames = new ArrayBlockingQueue<>(this.QUEUE_SIZE);
//        this.audioFrames = new ArrayBlockingQueue<>(this.QUEUE_SIZE);
//        this.onFinish = onFinish;
//        if (stream == null) {
//            System.out.format("Stream is null%n");
//        } else {
//            try {
//
//                this.frameGrabber = new FFmpegFrameGrabber(stream);
//                this.process();
//            } catch (Exception var4) {
//                HollowCore.LOGGER.error("Something went wrong while trying to play a video file. Skipping the video. Sorry!");
//                var4.printStackTrace();
//                this.onClose();
//            }
//        }
//        init();
//    }
//
//
//    public VideoOverlay(String filename, Runnable onFinish) {
//        this.videoFrames = new ArrayBlockingQueue<>(this.QUEUE_SIZE);
//        this.audioFrames = new ArrayBlockingQueue<>(this.QUEUE_SIZE);
//        this.onFinish = onFinish;
//
//        try {
//            this.frameGrabber = new FFmpegFrameGrabber(filename);
//            this.process();
//        } catch (Exception var4) {
//            HollowCore.LOGGER.error("Something went wrong while trying to play a video file. Skipping the video. Sorry!");
//            var4.printStackTrace();
//            this.onClose();
//        }
//        init();
//
//    }
//
//    protected void init() {
//        this.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//        this.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//
//        if (this.frameGrabber == null) {
//            this.onClose();
//        }
//
//        this.onResize();
//    }
//
//    private void onResize() {
//        if (this.imageWidth != 0 && this.imageHeight != 0 && this.width != 0 && this.height != 0) {
//            float screenAspect = (float) this.width / (float) this.height;
//            float imageAspect = (float) this.imageWidth / (float) this.imageHeight;
//            this.x2 = (float) this.width * 3.0F;
//            this.y2 = (float) this.height * 3.0F;
//            float scale;
//            float scaledImageHeight;
//            float heightFraction;
//            float heightU;
//            float dY;
//            if (imageAspect > screenAspect) {
//                scale = (float) this.width / (float) this.imageWidth;
//                scaledImageHeight = (float) this.imageHeight * scale;
//                heightFraction = scaledImageHeight / (float) this.height;
//                heightU = 3.0F * imageAspect / screenAspect;
//                dY = imageAspect / screenAspect * (1.0F - heightFraction) / 2.0F;
//                this.minU = 0.0F;
//                this.minV = 0.0F - dY;
//                this.maxU = 3.0F;
//                this.maxV = heightU - dY;
//            } else {
//                scale = (float) this.height / (float) this.imageHeight;
//                scaledImageHeight = (float) this.imageWidth * scale;
//                heightFraction = scaledImageHeight / (float) this.width;
//                heightU = 3.0F * screenAspect / imageAspect;
//                dY = screenAspect / imageAspect * (1.0F - heightFraction) / 2.0F;
//                this.minU = 0.0F - dY;
//                this.minV = 0.0F;
//                this.maxU = heightU - dY;
//                this.maxV = 3.0F;
//            }
//
//        } else {
//            this.x2 = 3.0F;
//            this.y2 = 3.0F;
//            this.maxU = 3.0F;
//            this.maxV = 3.0F;
//        }
//    }
//
//    private void process() {
//        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
//        if (this.frameGrabber == null) {
//            this.onClose();
//        } else {
//            System.out.println("Media Player starting!");
//
//            try {
//                this.frameGrabber.start();
//                if (this.frameGrabber.hasAudio()) {
//                    this.oneAudioFrameTimeNanoSeconds = 1000000000 / this.frameGrabber.getSampleRate();
//                    AudioFormat audioFormat = new AudioFormat((float) this.frameGrabber.getSampleRate(), 16, this.frameGrabber.getAudioChannels(), true, true);
//                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
//                    this.soundLine = (SourceDataLine) AudioSystem.getLine(info);
//                    this.soundLine.open(audioFormat);
//                    this.soundLine.start();
//                }
//
//                if (this.frameGrabber.hasVideo()) {
//                    this.imageWidth = this.frameGrabber.getImageWidth();
//                    this.imageHeight = this.frameGrabber.getImageHeight();
//                    this.onResize();
//                    this.oneVideoFrameTimeNanoSeconds = (long) (1.0E9D / this.frameGrabber.getFrameRate());
//                    this.frameTimestampToleranceMicroSeconds = (int) ((float) this.oneVideoFrameTimeNanoSeconds / 3000.0F);
//                    this.glTextureId = GL42.glGenTextures();
//                    GL42.glBindTexture(3553, this.glTextureId);
//                    GL42.glTexParameteri(3553, 10240, 9729);
//                    GL42.glTexParameteri(3553, 10241, 9729);
//                    GL42.glTexParameteri(3553, 33084, 0);
//                    GL42.glTexParameteri(3553, 33085, 0);
//                    GL42.glTexParameteri(3553, 10242, 33069);
//                    GL42.glTexParameteri(3553, 10243, 33069);
//                    GL42.glTexImage2D(3553, 0, 32849, this.imageWidth, this.imageHeight, 0, 32992, 5121, (ByteBuffer) null);
//                    this.glPixelBufferId = GL42.glGenBuffers();
//                    GL42.glBindBuffer(35052, this.glPixelBufferId);
//                    GL42.glBufferData(35052, (long) this.imageWidth * this.imageHeight * 3, 35040);
//                    GL42.glBindBuffer(35052, 0);
//                }
//            } catch (Exception var3) {
//                var3.printStackTrace();
//                this.onClose();
//            }
//
//        }
//    }
//
//    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//        if (!this.finished) {
//            try {
//                boolean rendered = false;
//                Frame capturedFrame;
//                int l = 7;
//
//                do {
//                    do {
//                        if (l <= 0 || rendered) {
//                            if (!rendered) {
//                                this.renderTexture(matrixStack);
//                            }
//
//                            return;
//                        }
//
//                        --l;
//                        if (!this.finishedGrabbing && (this.frameGrabber.hasAudio() && this.audioFrames.size() < 100 || this.frameGrabber.hasVideo() && this.videoFrames.size() < 100)) {
//                            capturedFrame = this.frameGrabber.grab();
//                            if (capturedFrame == null) {
//                                this.finishedGrabbing = true;
//                            } else {
//                                if (capturedFrame.samples != null) {
//                                    ++this.audioFrameCount;
//                                    this.audioFrames.add(capturedFrame.clone());
//                                }
//
//                                if (capturedFrame.image != null) {
//                                    ++this.videoFrameCount;
//                                    this.videoFrames.add(capturedFrame.clone());
//                                }
//
//                                capturedFrame.close();
//                            }
//                        }
//
//                        if ((!this.frameGrabber.hasVideo() || this.videoFrames.size() > 50) && (!this.frameGrabber.hasAudio() || this.audioFrames.size() > 50) && this.startTime == 0L) {
//                            this.startTime = System.nanoTime();
//                        }
//                    } while (this.startTime == 0L);
//
//                    Frame audioFrame = this.audioFrames.peek();
//                    if (audioFrame != null) {
//                        if (System.nanoTime() - this.startTime - audioFrame.timestamp * 1000L > 1000L * this.oneAudioFrameTimeNanoSeconds) {
//                            this.startTime += System.nanoTime() - this.startTime - audioFrame.timestamp * 1000L;
//                        }
//
//                        if (audioFrame.timestamp * 1000L <= System.nanoTime() - this.startTime) {
//                            audioFrame = this.audioFrames.poll();
//                            ShortBuffer channelSamplesShortBuffer;
//                            if (audioFrame != null) {
//                                channelSamplesShortBuffer = (ShortBuffer) audioFrame.samples[0];
//
//                                if (channelSamplesShortBuffer != null) {
//                                    channelSamplesShortBuffer.rewind();
//                                    ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);
//                                    outBuffer.asShortBuffer().put(channelSamplesShortBuffer);
//
//                                    audioFrame.close();
//                                    this.soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
//                                }
//                            }
//                        }
//                    }
//
//                    Frame videoFrame = this.videoFrames.peek();
//                    if (videoFrame != null) {
//                        if (System.nanoTime() - this.startTime - videoFrame.timestamp * 1000L > 10L * this.oneVideoFrameTimeNanoSeconds) {
//                            this.startTime += System.nanoTime() - this.startTime - videoFrame.timestamp * 1000L;
//                        }
//
//                        while (System.nanoTime() - this.startTime - videoFrame.timestamp * 1000L > this.oneVideoFrameTimeNanoSeconds) {
//                            this.videoFrames.poll();
//                            videoFrame.close();
//                            videoFrame = this.videoFrames.peek();
//                            if (videoFrame == null) break;
//                        }
//                        if (videoFrame == null) break;
//                        if ((videoFrame.timestamp - (long) this.frameTimestampToleranceMicroSeconds) * 1000L <= System.nanoTime() - this.startTime) {
//                            videoFrame = this.videoFrames.poll();
//                            if (videoFrame == null) break;
//                            long timeDiff = System.nanoTime() - this.startTime - videoFrame.timestamp * 1000L;
//                            timeDiff /= -1000000L;
//                            if (timeDiff > 0L && timeDiff < 40L) {
//                                Thread.sleep(timeDiff);
//                            }
//
//                            this.updateTexture((ByteBuffer) videoFrame.image[0]);
//                            videoFrame.close();
//                            this.renderTexture(matrixStack);
//                            rendered = true;
//
//                        }
//                    }
//                } while (!this.finishedGrabbing || !this.videoFrames.isEmpty() || !this.audioFrames.isEmpty());
//
//                System.out.format("Done! Found %d / %d frames (audio / images)%n", this.audioFrameCount, this.videoFrameCount);
//                System.out.format("start %d end %d duration %d%n", this.startTime, System.nanoTime(), this.startTime - System.nanoTime());
//                this.onClose();
//            } catch (Exception var12) {
//                var12.printStackTrace();
//                this.onClose();
//            }
//        }
//    }
//
//    private void updateTexture(ByteBuffer image) {
//        GL42.glBindTexture(3553, this.glTextureId);
//        GL42.glBindBuffer(35052, this.glPixelBufferId);
//        this.pixelData = GL15C.glMapBuffer(35052, 35001, image.limit(), this.pixelData);
//        GL42.glPixelStorei(3314, 0);
//        GL42.glPixelStorei(32878, 0);
//        GL42.glPixelStorei(3315, 0);
//        GL42.glPixelStorei(3316, 0);
//        GL42.glPixelStorei(32877, 0);
//        GL42.glPixelStorei(3317, 1);
//        this.pixelData.put(image);
//        GL42.glUnmapBuffer(35052);
//        GL42.glTexSubImage2D(3553, 0, 0, 0, this.imageWidth, this.imageHeight, 32992, 5121, 0L);
//        GL42.glBindBuffer(35052, 0);
//        GL42.glBindTexture(3553, 0);
//    }
//
//    public void onClose() {
//        this.finished = true;
//
//        try {
//            if (this.frameGrabber != null) {
//                this.frameGrabber.stop();
//                this.frameGrabber = null;
//            }
//
//            if (this.soundLine != null) {
//                this.soundLine.stop();
//                this.soundLine = null;
//            }
//
//            RenderSystem.deleteTexture(this.glTextureId);
//            GL42.glDeleteBuffers(this.glPixelBufferId);
//        } catch (Exception var2) {
//            var2.printStackTrace();
//        }
//
//        if (this.onFinish != null) {
//            this.onFinish.run();
//        }
//
//    }
//
//    private void renderTexture(MatrixStack matrixStack) {
//        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
//        RenderSystem.bindTexture(this.glTextureId);
//        Matrix4f matrix = matrixStack.last().pose();
//        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//        bufferbuilder.begin(4, DefaultVertexFormats.POSITION_TEX);
//        float blitOffset = 0.0F;
//        float x1 = 0.0F;
//        bufferbuilder.vertex(matrix, x1, this.y2, blitOffset).uv(this.minU, this.maxV).endVertex();
//        float y1 = 0.0F;
//        bufferbuilder.vertex(matrix, this.x2, y1, blitOffset).uv(this.maxU, this.minV).endVertex();
//        bufferbuilder.vertex(matrix, x1, y1, blitOffset).uv(this.minU, this.minV).endVertex();
//        bufferbuilder.end();
//        WorldVertexBufferUploader.end(bufferbuilder);
//        RenderSystem.bindTexture(0);
//    }
//}
