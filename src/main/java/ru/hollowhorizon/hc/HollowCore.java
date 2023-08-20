package ru.hollowhorizon.hc;

import com.modularmods.mcgltf.MCglTF;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.hc.api.registy.HollowMod;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.graphics.GPUMemoryManager;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.client.render.OpenGLUtils;
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer;
import ru.hollowhorizon.hc.client.utils.HollowKeyHandler;
import ru.hollowhorizon.hc.client.utils.HollowPack;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;
import ru.hollowhorizon.hc.common.commands.HollowCommands;
import ru.hollowhorizon.hc.common.handlers.DelayHandler;
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.registry.HollowModProcessor;
import ru.hollowhorizon.hc.common.registry.ModEntities;
import ru.hollowhorizon.hc.common.registry.ModShaders;
import ru.hollowhorizon.hc.common.registry.RegistryLoader;


@HollowMod(HollowCore.MODID)
@Mod(HollowCore.MODID)
public class HollowCore {
    public static final String MODID = "hc";
    public static final Logger LOGGER = LoggerLoader.createLogger("HollowLogger");
    @HollowConfig(value = "general/debug_mode", description = "Enables debug mode, logs, and some more info for developers.")
    public static final boolean DEBUG_MODE = true;

    public HollowCore() {
        new MCglTF();
        HollowModProcessor.initMod();
        LOGGER.info("Starting HollowCore...");

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::loadEnd);
        modBus.addListener(this::onAttribute);
        modBus.addListener(this::onResourcePackAdd);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        //GltfModelSources.INSTANCE.addSource(new PathSource(FMLPaths.GAMEDIR.get().resolve("hollowengine")));

        if (FMLEnvironment.dist.isClient()) {
            OpenGLUtils.init();
            //клавиши
            forgeBus.register(new HollowKeyHandler());
            forgeBus.addListener(HollowKeyHandler::onKeyInput);

            //события
            forgeBus.addListener(ClientTickHandler::clientTickEnd);

            //модели
            //new GlTFModelManager();
            //modBus.addListener(GlTFModelManager::clientSetup);
            modBus.addListener(this::onRendererCreating);

            GPUMemoryManager.Companion.getInstance().initialize();

            forgeBus.addListener(ModShaders::init);
        }
        new HollowEventHandler().init();
        DelayHandler.init();
        //команды
        forgeBus.addListener(this::registerCommands);

        //мод
        forgeBus.register(this);

        forgeBus.addGenericListener(Entity.class, HollowCapabilityStorageV2::registerProvidersEntity);
        forgeBus.addGenericListener(BlockEntity.class, HollowCapabilityStorageV2::registerProvidersBlockEntity);
        forgeBus.addGenericListener(Level.class, HollowCapabilityStorageV2::registerProvidersWorld);

        forgeBus.addListener(this::configSave);

        RegistryLoader.registerAll();
    }

    public void onResourcePackAdd(AddPackFindersEvent event) {
        event.addRepositorySource((adder, creator) -> {
            var pack = HollowPack.getPackInstance();
            adder.accept(
                    creator.create(
                            pack.getName(),
                            Component.literal(pack.getName()),
                            true,
                            () -> pack,
                            new PackMetadataSection(
                                    Component.translatable("fml.resources.modresources", 1),
                                    PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
                            ),
                            Pack.Position.TOP,
                            PackSource.BUILT_IN,
                            pack.isHidden()
                    )
            );
        });
    }


    //『Pre-Init』
    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();

    }

    private void onAttribute(EntityAttributeCreationEvent event) {
        event.put(ModEntities.INSTANCE.getTEST_ENTITY().get(), TestEntity.createMobAttributes().build());
        //event.put(ModEntities.TEST_ENTITY_V2, TestEntity.createMobAttributes().build());
    }

    @OnlyIn(Dist.CLIENT)
    private void onRendererCreating(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INSTANCE.getTEST_ENTITY().get(), GLTFEntityRenderer::new);
    }

    private void configSave(ServerStoppedEvent event) {
        HollowCoreConfig.save();
    }

    //『Post-Init』
    private void loadEnd(final FMLLoadCompleteEvent event) {

    }

    //『server』

    private void registerCommands(RegisterCommandsEvent event) {
        HollowCommands.register(event.getDispatcher());
    }


}
