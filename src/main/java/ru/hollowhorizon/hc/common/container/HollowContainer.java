package ru.hollowhorizon.hc.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.hc.api.registy.HollowPacket;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.events.UniversalContainerEvent;
import ru.hollowhorizon.hc.common.network.UniversalPacket;
import ru.hollowhorizon.hc.common.registry.ModContainers;

import java.util.List;

public class HollowContainer extends Container {
    protected final PlayerInventory playerInventory;
    private UniversalContainer container;
    private List<ItemStack> waitItems;

    public HollowContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
        this(inv, windowId);
    }

    public HollowContainer(PlayerInventory playerInventory, int windowId) {
        super(ModContainers.HOLLOW_CONTAINER, windowId);
        this.playerInventory = playerInventory;
    }

    @Override
    public void setAll(List<ItemStack> p_190896_1_) {
        if(container != null) {
            super.setAll(p_190896_1_);
        } else {
            waitItems = p_190896_1_;
        }
    }

    public UniversalContainer getUContainer() {
        return container;
    }

    public void setUContainer(UniversalContainer container) {
        this.container = container;
        this.container.serverInit(this);
        if(this.playerInventory.player.level.isClientSide) setAll(waitItems);
    }

    public Slot createSlot(Slot slot) {
        return this.addSlot(slot);
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return true;
    }
}
