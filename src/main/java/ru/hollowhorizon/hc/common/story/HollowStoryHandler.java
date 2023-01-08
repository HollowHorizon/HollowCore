package ru.hollowhorizon.hc.common.story;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2;
import ru.hollowhorizon.hc.common.capabilities.HollowStoryCapability;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.*;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;

public abstract class HollowStoryHandler {
    public PlayerEntity player;

    public void start(PlayerEntity player) {
        MinecraftForge.EVENT_BUS.register(this);
        this.player = player;
        this.player.getCapability(HollowCapabilityV2.Companion.get(HollowStoryCapability.class)).ifPresent((capability) -> {
            String storyName = getStoryName();
            if (capability.hasStory(storyName)) {
                CompoundNBT story = capability.getStory(storyName);
                loadNBT(story);
            } else {
                capability.addStory(storyName, saveNBT());
            }
        });
        if(enableClient()) {
            if(!FMLEnvironment.dist.isClient()) {
                NetworkHandler.sendMessageToClient(new StartStoryEventToClient(getStoryName()), this.player);
            }
        }
    }

    public CompoundNBT saveNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("story_name", getStoryName());
        return nbt;
    }

    public void loadNBT(CompoundNBT nbt) {

    }

    public void setChanged() {
        if(!FMLEnvironment.dist.isClient()) {
            NetworkHandler.sendMessageToClient(new UpdateStoryEventToClient(saveNBT()), this.player);
        } else {
            NetworkHandler.sendMessageToServer(new UpdateStoryEventToServer(saveNBT()));
        }
    }

    public void saveStory() {
        player.getCapability(HollowCapabilityV2.Companion.get(HollowStoryCapability.class)).ifPresent((capability) -> capability.addStory(getStoryName(), saveNBT()));
    }

    public void loadStory() {
        player.getCapability(HollowCapabilityV2.Companion.get(HollowStoryCapability.class)).ifPresent((capability) -> loadNBT(capability.getStory(getStoryName())));
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getPlayer().getUUID().equals(this.player.getUUID())) {
            saveStory();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public void stop() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.player.getCapability(HollowCapabilityV2.Companion.get(HollowStoryCapability.class)).ifPresent((capability) -> capability.removeStory(getStoryName()));
        if(enableClient()) {
            if(!FMLEnvironment.dist.isClient()) {
                NetworkHandler.sendMessageToClient(new StopStoryEventToClient(getStoryName()), this.player);
            } else {
                NetworkHandler.sendMessageToServer(new StopStoryEventToServer(getStoryName()));
            }
        }
    }

    public boolean enableClient() {
        return false;
    }

    public abstract String getStoryName();
}
