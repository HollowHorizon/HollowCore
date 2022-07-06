package ru.hollowhorizon.hc.common.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import ru.hollowhorizon.hc.api.registy.HollowPacket;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.events.UniversalContainerEvent;
import ru.hollowhorizon.hc.common.network.UniversalPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class UniversalContainer {
    @HollowPacket
    public static final UniversalPacket<CompoundNBT> HOLLOW_CONTAINER_PACKET = new UniversalPacket<CompoundNBT>() {
        @Override
        public HollowNBTSerializer<CompoundNBT> serializer() {
            return NBTUtils.COMPOUND_SEIALIZER;
        }

        @Override
        public void onReceived(PlayerEntity player, CompoundNBT value) {
            MinecraftForge.EVENT_BUS.post(new UniversalContainerEvent(value.getString("_container_name"), value));
        }
    };
    private HollowContainer container;

    public UniversalContainer() {
    }

    public static void openContainer(String containerName, ServerPlayerEntity player) {
        UniversalContainer container = UniversalContainerManager.getContainer(containerName);
        player.openMenu(new UniversalContainerProvider<>(container));
        HOLLOW_CONTAINER_PACKET.sendToClient(player, container.writeNBT(new CompoundNBT()));
    }

    public abstract ITextComponent containerName();

    public void serverInit(HollowContainer container) {

        container.setUContainer(this);
    }

    public void readNBT(CompoundNBT nbt) {
    }

    public CompoundNBT writeNBT(CompoundNBT nbt) {
        nbt.putString("_container_name", UniversalContainerManager.getContainerId(this));
        return nbt;
    }

    @OnlyIn(Dist.CLIENT)
    public void clientInit(ContainerScreen<HollowContainer> screen) {
    }

    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, int mouseX, int mouseY) {
    }

    private static class UniversalContainerProvider<T extends UniversalContainer> implements INamedContainerProvider {
        private final T container;

        private UniversalContainerProvider(T container) {
            this.container = container;
        }

        @Nonnull
        @Override
        public ITextComponent getDisplayName() {
            return container.containerName();
        }

        @Nullable
        @Override
        public Container createMenu(int i, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
            HollowContainer container = new HollowContainer(playerInventory, i);
            container.setUContainer(this.container);
            return container;
        }
    }
}
