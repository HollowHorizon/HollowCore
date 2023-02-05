package ru.hollowhorizon.hc;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.resources.IResourcePack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.hc.api.registy.HollowMod;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.client.utils.HollowKeyHandler;
import ru.hollowhorizon.hc.client.utils.HollowPack;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat;
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormatKt;
import ru.hollowhorizon.hc.common.animations.AnimationManager;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;
import ru.hollowhorizon.hc.common.commands.HollowCommands;
import ru.hollowhorizon.hc.common.handlers.DelayHandler;
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;
import ru.hollowhorizon.hc.common.registry.ModEntities;
import ru.hollowhorizon.hc.common.registry.ModStructurePieces;
import ru.hollowhorizon.hc.common.registry.ModStructures;
import ru.hollowhorizon.hc.common.scripting.HSCompiler;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;
import ru.hollowhorizon.hc.common.world.storage.HollowWorldData;
import ru.hollowhorizon.hc.proxy.ClientProxy;
import ru.hollowhorizon.hc.proxy.CommonProxy;
import ru.hollowhorizon.hc.proxy.ServerProxy;

import java.util.ArrayList;

import static ru.hollowhorizon.hc.common.objects.entities.data.AnimationDataParametersKt.ANIMATION_MANAGER;

@HollowMod(HollowCore.MODID)
@Mod(HollowCore.MODID)
public class HollowCore {
    public static final String MODID = "hc";
    public static final Logger LOGGER = LoggerLoader.createLogger("HollowLogger");
    @HollowConfig(value = "general/debug_mode", description = "Enables debug mode, logs, and some more info for developers.")
    public static final boolean DEBUG_MODE = true;
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(
            () -> ClientProxy::new,
            () -> ServerProxy::new
    );

    public HollowCore() {
        new GlTFModelManager();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::loadEnd);
        modBus.addListener(this::onAttribute);
        modBus.addListener(GlTFModelManager::clientSetup);
        modBus.addListener(GlTFModelManager::modelBake);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        //Обработчик NBT первый раз грузится долго, поэтому его лучше загрузить в отдельном потоке
        NBTFormat.Default.init();
        HSCompiler.init();

        if (proxy.isClientSide()) {
            //клавиши
            forgeBus.register(new HollowKeyHandler());
            forgeBus.addListener(HollowKeyHandler::onKeyInput);

            //события
            forgeBus.addListener(ClientTickHandler::clientTickEnd);
        }
        new HollowEventHandler().init();
        DelayHandler.init();
        //команды
        forgeBus.addListener(this::registerCommands);
        forgeBus.addListener(AnimationManager::tick);

        //структуры
        forgeBus.addListener(EventPriority.HIGHEST, ModStructures::onBiomeLoad);

        //мод
        forgeBus.register(this);

        forgeBus.addGenericListener(Entity.class, HollowCapabilityStorageV2::registerProvidersEntity);
        forgeBus.addGenericListener(TileEntity.class, HollowCapabilityStorageV2::registerProvidersTile);
        forgeBus.addGenericListener(Chunk.class, HollowCapabilityStorageV2::registerProvidersChunk);
        forgeBus.addGenericListener(World.class, HollowCapabilityStorageV2::registerProvidersWorld);

        forgeBus.addListener(this::configSave);
    }

    public static void onResourcePackAdd(ArrayList<IResourcePack> packs) {
        packs.add(HollowPack.getPackInstance());
    }


    //『Pre-Init』
    private void setup(final FMLCommonSetupEvent event) {
        proxy.init();

        ModCapabilities.init();
        NetworkHandler.register();
        DataSerializers.registerSerializer(ANIMATION_MANAGER);

        event.enqueueWork(ModStructures::postInit);
        event.enqueueWork(ModStructurePieces::registerPieces);

        NBTUtils.init();
    }

    private void onAttribute(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TEST_ENTITY, TestEntity.createMobAttributes().build());
    }


    private void configSave(FMLServerStoppedEvent event) {
        HollowCoreConfig.save();
    }

    //『Post-Init』
    private void loadEnd(final FMLLoadCompleteEvent event) {
        StoryEventListener.init();
    }

    //『server』
    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        HollowWorldData.INSTANCE = event.getServer().overworld()
                .getChunkSource().getDataStorage().computeIfAbsent(HollowWorldData::new, "hollow_world_data");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        HollowCommands.register(event.getDispatcher());
    }


}
