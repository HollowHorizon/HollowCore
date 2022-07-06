package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.common.container.HollowContainer;
import ru.hollowhorizon.hc.common.container.UniversalContainer;
import ru.hollowhorizon.hc.common.container.UniversalContainerManager;
import ru.hollowhorizon.hc.common.events.UniversalContainerEvent;

public class HollowContainerScreen extends ContainerScreen<HollowContainer> {
    public HollowContainerScreen(HollowContainer p_i51105_1_, PlayerInventory p_i51105_2_, ITextComponent p_i51105_3_) {
        super(p_i51105_1_, p_i51105_2_, p_i51105_3_);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerData(UniversalContainerEvent event) {
        UniversalContainer container = UniversalContainerManager.getContainer(event.getContainerName());
        this.menu.setUContainer(container);
        init();
    }

    @Override
    public void onClose() {

        super.onClose();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void init() {
        if (this.menu.getUContainer() == null) return;

        super.init();
        this.menu.getUContainer().clientInit(this);
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);

        if (this.menu.getUContainer() == null) return;

        this.menu.getUContainer().render(p_230430_1_, p_230430_2_, p_230430_3_);
    }

    @Override
    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
    }
}
