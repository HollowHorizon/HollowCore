package ru.hollowhorizon.hc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.hc.api.registy.HollowMod;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.client.hollow_config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.utils.HollowKeyHandler;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.client.video.MediaListener;
import ru.hollowhorizon.hc.common.animations.AnimationManager;
import ru.hollowhorizon.hc.common.commands.HollowCommands;
import ru.hollowhorizon.hc.common.container.TestUContainer;
import ru.hollowhorizon.hc.common.container.UniversalContainerManager;
import ru.hollowhorizon.hc.common.handlers.DelayHandler;
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler;
import ru.hollowhorizon.hc.common.integration.ftb.quests.FTBQuestsHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.registry.*;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;
import ru.hollowhorizon.hc.common.world.storage.HollowWorldData;
import ru.hollowhorizon.hc.proxy.ClientProxy;
import ru.hollowhorizon.hc.proxy.CommonProxy;
import ru.hollowhorizon.hc.proxy.ServerProxy;

@HollowMod
@Mod(HollowCore.MODID)
public class HollowCore {
    public static final String MODID = "hc";
    public static final Logger LOGGER = LogManager.getLogger();
    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public HollowCore() {
        VideoRegistry.init();
        if (ModList.get().isLoaded("ftbquests")) {
            FTBQuestsHandler.init();
        }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModParticles::onRegisterParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadEnd);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        if (proxy.isClientSide()) {
            //??????????????
            forgeBus.register(new HollowKeyHandler());
            forgeBus.addListener(HollowKeyHandler::onKeyInput);

            //??????????????
            forgeBus.addListener(ClientTickHandler::clientTickEnd);
            new HollowEventHandler().init();
            HollowCoreConfig.initConfig();
            MediaListener.registerReload();
        }
        DelayHandler.init();
        //??????????????
        forgeBus.addListener(this::registerCommands);
        forgeBus.addListener(AnimationManager::tick);

        //??????????????????
        forgeBus.addListener(EventPriority.HIGHEST, ModStructures::onBiomeLoad);

        //??????
        forgeBus.register(this);
        forgeBus.addListener(this::registerReloadListeners);
        forgeBus.addGenericListener(Entity.class, ModCapabilities::attachCapabilityToEntity);


    }

    public void registerReloadListeners(AddReloadListenerEvent e) {
        if (FMLEnvironment.dist.isClient()) {
            //ModShaders.init(e);
        }
    }

    //???Pre-Init???
    private void setup(final FMLCommonSetupEvent event) {
        proxy.init();

        ModCapabilities.init();
        NetworkHandler.register();

        event.enqueueWork(ModStructures::postInit);
        event.enqueueWork(ModStructurePieces::registerPieces);

        NBTUtils.init();

        GlobalEntityTypeAttributes.put(ModEntities.testEntity, TestEntity.createMobAttributes().build());

        UniversalContainerManager.registerContainer("test_container", TestUContainer::new);
    }

    //???Post-Init???
    private void loadEnd(final FMLLoadCompleteEvent event) {
        StoryEventListener.init();
    }

    //???server???
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        HollowWorldData.INSTANCE = event.getServer().overworld().getChunkSource().getDataStorage().computeIfAbsent(HollowWorldData::new, "hollow_world_data");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        HollowCommands.register(event.getDispatcher());
    }


}
