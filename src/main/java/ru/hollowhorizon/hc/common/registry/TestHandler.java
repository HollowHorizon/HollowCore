package ru.hollowhorizon.hc.common.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.api.registy.StoryObject;
import ru.hollowhorizon.hc.client.screens.CameraScreen;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;

@StoryObject
public class TestHandler extends HollowStoryHandler {
    private int time = 0;

    @Override
    public void start(PlayerEntity player) {
        super.start(player);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (time < 1000) time++;
            else {
                setChanged();
            }

            if (time % 20 == 0) {
                player.sendMessage(new StringTextComponent(time / 20 + ""), player.getUUID());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void ctick(TickEvent.ClientTickEvent event) {
        if(time > 999) {
            Minecraft.getInstance().setScreen(new CameraScreen());
            stop();
        }
    }

    @Override
    public CompoundNBT saveNBT() {
        CompoundNBT nbt = super.saveNBT();
        nbt.putInt("state", time);
        return nbt;
    }

    @Override
    public void loadNBT(CompoundNBT nbt) {
        super.loadNBT(nbt);
        time = nbt.getInt("state");
    }

    @Override
    public String getStoryName() {
        return "test.test";
    }
}
