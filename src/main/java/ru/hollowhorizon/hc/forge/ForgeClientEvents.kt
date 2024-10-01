package ru.hollowhorizon.hc.forge

//? if forge && >=1.21 {
/*import net.minecraft.world.item.Item
*///?}

//? if forge {

/*import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.*

object ForgeClientEvents {
    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeClientEvents::registerShaders)
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeClientEvents::onRegisterKeys)
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeClientEvents::onEntityRenderers)
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeClientEvents::registerReloadListeners)
        MinecraftForge.EVENT_BUS.addListener(ForgeClientEvents::onClientTick)
        MinecraftForge.EVENT_BUS.addListener(ForgeClientEvents::onRenderTooltips)
    }

    private fun onEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        RegisterEntityRenderersEvent(event::registerEntityRenderer).post()
        RegisterBlockEntityRenderersEvent(event::registerBlockEntityRenderer).post()
    }


    private fun registerReloadListeners(event: RegisterClientReloadListenersEvent) {
        val hcevent = RegisterReloadListenersEvent.Client()
        EventBus.post(hcevent)
        hcevent.listeners.forEach {
            event.registerReloadListener(it)
        }
    }

    private fun registerShaders(event: net.minecraftforge.client.event.RegisterShadersEvent) {
        val hcEvent = RegisterShadersEvent()
        EventBus.post(hcEvent)
        hcEvent.shaders.forEach {
            event.registerShader(ShaderInstance(event.resourceProvider, it.key, it.value.first), it.value.second)
        }
    }

    private fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return

        EventBus.post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Client(
                Minecraft.getInstance()
            )
        )
    }

    private fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        RegisterKeyBindingsEvent(event::register).post()
    }

    private fun onRenderTooltips(event: net.minecraftforge.event.entity.player.ItemTooltipEvent) {
        ru.hollowhorizon.hc.common.events.client.ItemTooltipEvent(
            event.flags,
            event.itemStack,
            event.toolTip,
            //? if >=1.21 {
            /^Item.TooltipContext.of(Minecraft.getInstance().level)
            ^///?}
        ).post()
    }
}
*///?}

//? if forge && <=1.19.2 {
/*val net.minecraftforge.client.event.RegisterShadersEvent.resourceProvider get() = resourceManager
*///?}