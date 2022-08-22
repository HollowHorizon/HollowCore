package ru.hollowhorizon.hc.common.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.screens.DialogueOptionsScreen;
import ru.hollowhorizon.hc.client.screens.DialogueScreen;
import ru.hollowhorizon.hc.common.animations.CutsceneStartHandler;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilities;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class HollowEventHandler {
    @HollowConfig("enable_blur")
    public static boolean ENABLE_BLUR = true;

    public static final List<String> BLUR_WHITELIST = new ArrayList<>();
    private Framebuffer framebuffer;

    private static void drawQuad() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(-1, -1, 0).uv(0, 0).endVertex();
        buffer.vertex(1, -1, 0).uv(1, 0).endVertex();
        buffer.vertex(1, 1, 0).uv(1, 1).endVertex();
        buffer.vertex(-1, 1, 0).uv(0, 1).endVertex();
        tessellator.end();
    }

    public void init() {
        BLUR_WHITELIST.add(DialogueScreen.class.getName());
        BLUR_WHITELIST.add(DialogueOptionsScreen.class.getName());

        if (HollowCore.proxy.isClientSide()) {
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlay);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlayPost);
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::tooltipEvent);
    }

    private void tooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation location = stack.getItem().getRegistryName();
        if (location == null) return;
        TranslationTextComponent text = new TranslationTextComponent("item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc");
        if (!text.getString().equals("item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc")) {
            event.getToolTip().add(text);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            for (Capability<HollowCapability<?>> cap : HollowCapabilities.CAPABILITIES.values()) {
                HollowCapability<?> origCap = event.getOriginal().getCapability(cap).orElse(null);
                HollowCapability<?> newCap = event.getPlayer().getCapability(cap).orElse(null);
                newCap.readNBT(origCap.writeNBT());
                newCap.update(event.getPlayer());
                newCap.onDeath(event.getPlayer(), event.getOriginal());
            }
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        CutsceneStartHandler.startUncompletedCutscene(player);

        StoryEventStarter.startAll(player);

        //update capabilities on clients
        for (Capability<HollowCapability<?>> cap : HollowCapabilities.CAPABILITIES.values()) {
            player.getCapability(cap).ifPresent(capability -> capability.update(event.getPlayer()));
        }
    }


    private void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (Minecraft.getInstance().screen != null) {
            if (BLUR_WHITELIST.contains(Minecraft.getInstance().screen.getClass().getName()) && ENABLE_BLUR) {
                if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                    return;
                }

                event.setCanceled(true);

                if (!event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
                    return;
                }

                Minecraft.getInstance().options.setCameraType(PointOfView.FIRST_PERSON);

                Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(MODID, "shaders/blur.json"));
            }
        }
    }

    private void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {

    }

    public void drawColouredRect(int posX, int posY, int xSize, int ySize, int colour) {
        drawRect(posX, posY, posX + xSize, posY + ySize, colour, colour, 1F, 0);
    }

    public void drawRect(float left, float top, float right, float bottom, int colour1, int colour2, float fade, double zLevel) {
        float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
        float f1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float f2 = (float) (colour1 >> 8 & 255) / 255.0F;
        float f3 = (float) (colour1 & 255) / 255.0F;
        float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
        float f5 = (float) (colour2 >> 16 & 255) / 255.0F;
        float f6 = (float) (colour2 >> 8 & 255) / 255.0F;
        float f7 = (float) (colour2 & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.vertex(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.vertex(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.vertex(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.end();

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private void renderShaders(Framebuffer framebuffer, int screenWidth, int screenHeight) {

    }
}