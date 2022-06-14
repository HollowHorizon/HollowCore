package ru.hollowhorizon.hc.client.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.api.registy.StoryObject;
import ru.hollowhorizon.hc.client.render.shader.SwirlTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ShaderHandler {
    public static final ShaderHandler INSTANCE = new ShaderHandler();

    private SwirlTest swirlEffect;
    private Framebuffer blitBuffer;
    private float lastSwirlAngle = 0.0F;
    private float swirlAngle = 0.0F;

    public void init(AddReloadListenerEvent e) {
        this.swirlEffect = new SwirlTest().init();

        this.blitBuffer = new Framebuffer(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, true, Minecraft.ON_OSX);

    }

    public float getSwirlAngle(float partialTicks) {
        return this.lastSwirlAngle + (this.swirlAngle - this.lastSwirlAngle) * partialTicks;
    }

    public void setSwirlAngle(float swirlAngle) {
        this.lastSwirlAngle = this.swirlAngle;
        this.swirlAngle = swirlAngle;
    }

    public void addSwirlAngle() {
        float swirl = getSwirlAngle(1);
        if(swirl < 2) {
            setSwirlAngle(swirl + (swirl * 0.055F) + 0.0005F);
        } else {
            setSwirlAngle(swirl + ((swirl * 0.055F) / (swirl - 1)) + 0.0005F);
        }
    }

    public void applySwirl(float partialTicks) {
        MainWindow window = Minecraft.getInstance().getWindow();
        GlStateManager._matrixMode(GL11.GL_PROJECTION);
        GlStateManager._loadIdentity();
        GlStateManager._ortho(0.0D, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager._matrixMode(GL11.GL_MODELVIEW);
        GlStateManager._loadIdentity();
        GlStateManager._translatef(0.0F, 0.0F, -2000.0F);

        Framebuffer mainFramebuffer = Minecraft.getInstance().getMainRenderTarget();
        Framebuffer blitFramebuffer = this.blitBuffer;

        if (mainFramebuffer.width != blitFramebuffer.width || mainFramebuffer.height != blitFramebuffer.height)
            blitFramebuffer.createBuffers(mainFramebuffer.width, mainFramebuffer.height, Minecraft.ON_OSX);


        //Render swirl
        this.swirlEffect.setAngle(ClientTickHandler.ticksInGame);
        this.swirlEffect.create(mainFramebuffer)
                .setSource(mainFramebuffer.getColorTextureId())
                .setBlitFramebuffer(blitFramebuffer)
                .setPreviousFramebuffer(mainFramebuffer)
                .render(partialTicks);
    }
}
