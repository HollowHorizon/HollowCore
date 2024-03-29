package ru.hollowhorizon.hc;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.hc.client.handlers.TickHandler;
import ru.hollowhorizon.hc.client.imgui.ImGuiExampleKt;
import ru.hollowhorizon.hc.client.imgui.ImguiLoader;
import ru.hollowhorizon.hc.client.imgui.ImguiLoaderKt;
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager;
import ru.hollowhorizon.hc.client.render.block.GLTFBlockEntityRenderer;
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer;
import ru.hollowhorizon.hc.client.render.shaders.post.PostChain;
import ru.hollowhorizon.hc.client.utils.HollowPack;
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage;
import ru.hollowhorizon.hc.common.commands.HollowCommands;
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.registry.*;
import ru.hollowhorizon.hc.particles.EffekseerParticles;
import thedarkcolour.kotlinforforge.forge.ForgeKt;


@Mod(HollowCore.MODID)
public class HollowCore {
    public static final String MODID = "hc";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean DEBUG_MODE = false;

    public HollowCore() {
        HollowModProcessor.initMod();
        LOGGER.info("Starting HollowCore...");

        IEventBus modBus = ForgeKt.getMOD_CONTEXT().getKEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::onAttribute);
        modBus.addListener(this::onResourcePackAdd);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        if (FMLEnvironment.dist.isClient()) {

            //события
            forgeBus.addListener(TickHandler::clientTickEnd);
            forgeBus.addListener(TickHandler::serverTickEnd);

            //модели
            modBus.addListener(GltfManager::onReload);
            modBus.addListener(PostChain::onReload);
            modBus.addListener(this::onRendererCreating);
            modBus.register(ModShaders.INSTANCE);
            modBus.addListener(ModParticles::onRegisterParticles);

            RenderSystem.recordRenderCall(() -> ImguiLoader.INSTANCE.onGlfwInit(Minecraft.getInstance().getWindow().getWindow()));
        }
        new HollowEventHandler().init();
        //команды
        forgeBus.addListener(this::registerCommands);

        //мод
        forgeBus.register(this);

        forgeBus.addGenericListener(Entity.class, CapabilityStorage::registerProvidersEntity);
        forgeBus.addGenericListener(BlockEntity.class, CapabilityStorage::registerProvidersBlockEntity);
        forgeBus.addGenericListener(Level.class, CapabilityStorage::registerProvidersWorld);
        if (ModList.get().isLoaded("ftbteams"))
            forgeBus.addGenericListener(Team.class, CapabilityStorage::registerProvidersTeam);

        /* MODULES */

        var particles = EffekseerParticles.INSTANCE; //Initialization of particles

        RegistryLoader.registerAll();
    }

    public void onResourcePackAdd(AddPackFindersEvent event) {
        event.addRepositorySource((adder, creator) -> {
            var pack = HollowPack.getPackInstance();
            adder.accept(creator.create(pack.getName(), Component.literal(pack.getName()), true, () -> pack, new PackMetadataSection(Component.translatable("fml.resources.modresources", 1), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())), Pack.Position.TOP, PackSource.BUILT_IN, pack.isHidden()));
        });
    }


    //『Pre-Init』
    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();

    }

    private void onAttribute(EntityAttributeCreationEvent event) {
        event.put(ModEntities.INSTANCE.getTEST_ENTITY().get(), TestEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2f).build());
    }

    @OnlyIn(Dist.CLIENT)
    private void onRendererCreating(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INSTANCE.getTEST_ENTITY().get(), GLTFEntityRenderer::new);

        event.registerBlockEntityRenderer(ModTileEntities.INSTANCE.getSAVE_OBELISK_TILE().get(), GLTFBlockEntityRenderer::new);
    }


    private void registerCommands(RegisterCommandsEvent event) {
        HollowCommands.register(event.getDispatcher());
    }

}
