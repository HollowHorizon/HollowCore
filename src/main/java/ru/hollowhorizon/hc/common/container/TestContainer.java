package ru.hollowhorizon.hc.common.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.common.container.inventory.HollowInventory;
import ru.hollowhorizon.hc.common.container.inventory.HollowSlot;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class TestContainer extends HollowContainer {

    @Override
    public void serverInit(ContainerManager manager) {
        manager.addPlayerHotBar(20, 30);
        manager.addPlayerInventory(20, 100);

        HollowInventory INPUT = manager.addInventory(new HollowInventory(),
                new HollowSlot(20, 120),
                new HollowSlot(20 + 18, 120),
                new HollowSlot(20 + 18 * 2, 120),
                new HollowSlot(20 + 18 * 3, 120),
                new HollowSlot(20 + 18 * 4, 120)
        );

        HollowInventory OUTPUT = manager.addInventory(new HollowInventory(),
                new HollowSlot(20 + 18 * 8, 120)
        );
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().textureManager.bind(new ResourceLocation(MODID, "textures/gui/test.png"));
        blit(stack, 0, 0, 400, 300);
    }
}
