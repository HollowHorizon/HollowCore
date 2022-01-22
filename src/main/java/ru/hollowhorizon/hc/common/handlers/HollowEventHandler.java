package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.render.OpenGLUtils;
import ru.hollowhorizon.hc.common.animations.CutsceneStartHandler;
import ru.hollowhorizon.hc.common.container.HollowContainer;
import ru.hollowhorizon.hc.common.network.data.ActionsData;
import ru.hollowhorizon.hc.common.network.data.LoreChoicesData;
import ru.hollowhorizon.hc.common.network.data.ReputationDataForPlayer;
import ru.hollowhorizon.hc.common.network.data.StoryInfoData;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.registry.ModEntities;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import static ru.hollowhorizon.hc.HollowCore.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class HollowEventHandler {
    public void init() {
        if (HollowCore.proxy.isClientSide()) {
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlay);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlayPost);
            MinecraftForge.EVENT_BUS.addListener(this::onModelRegistry);
            MinecraftForge.EVENT_BUS.addListener(this::renderWorldEvent);
        }
        MinecraftForge.EVENT_BUS.addListener(this::tooltipEvent);
    }

    @SubscribeEvent
    public void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        if (!Minecraft.getInstance().player.canSee(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity().getType().getRegistryName().equals(ModEntities.testEntity.getRegistryName())) {
            TestEntity entity = (TestEntity) event.getEntity();
            event.setCanceled(true);
            //renderer.render(entity, (LivingRenderer<TestEntity, PlayerModel<TestEntity>>) event.getRenderer(), event.getBuffers(), event.getMatrixStack(), event.getLight(), event.getPartialRenderTick());
        }
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

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        HollowCore.LOGGER.info("gui!!");
        LoreChoicesData.INSTANCE.createFile(player);
        StoryInfoData.INSTANCE.createFile(player);
        ReputationDataForPlayer.INSTANCE.createFile(player);
        ActionsData.INSTANCE.createFile(player);

        CutsceneStartHandler.startUncompletedCutscene(player);

        StoryEventStarter.startAll(player);
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("container.");
            }

            @Override
            public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new HollowContainer(i, playerInventory);
            }
        };
        NetworkHooks.openGui(player, containerProvider);
    }

    public void onModelRegistry(ModelRegistryEvent event) {
        OpenGLUtils.onModelRegistryEvent(event);
    }

    public void renderWorldEvent(RenderWorldLastEvent event) {
    }

    private void onRenderOverlay(TickEvent.RenderTickEvent event) {

    }

    private void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {

    }
}