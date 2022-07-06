package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import ru.hollowhorizon.hc.client.screens.util.Alignment;
import ru.hollowhorizon.hc.client.screens.util.WidgetPlacement;
import ru.hollowhorizon.hc.client.screens.widget.WidgetBox;
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton;
import ru.hollowhorizon.hc.client.screens.widget.list.ListWidget;

import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class TestScreen extends HollowScreen {
    public TestScreen(ITextComponent screenText) {
        super(screenText);
    }

    @Override
    protected void init() {
        List<Widget> list = new ArrayList<>();
        WidgetBox box = new WidgetBox(0, 0, 250, 80, 3, 1, 10, 0);

        box.putWidget(1, 1, (x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));
        box.putWidget(2, 1, (x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));
        box.putWidget(3, 1, (x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));

        list.add(box);
        list.add(new BaseButton(0, 0, 80, 20, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));
        list.add(new BaseButton(0, 0, 80, 30, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));
        list.add(new BaseButton(0, 0, 80, 40, "кынопка 1", b -> {
        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")));

        ListWidget listWidget = WidgetPlacement.configureWidget((x, y, w, h) -> new ListWidget(list, x, y, w, h),
                Alignment.BOTTOM_CENTER, 10, 0, this.width, this.height, 300, 100);

        this.addButton(listWidget);


        addButtons(
                WidgetPlacement.configureWidget((x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 1", b -> {
                        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")),
                        Alignment.LEFT_CENTER, 20, -40, this.width, this.height, 90, 20),
                WidgetPlacement.configureWidget((x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 2", b -> {
                        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")),
                        Alignment.LEFT_CENTER, 20, -10, this.width, this.height, 90, 20),
                WidgetPlacement.configureWidget((x, y, w, h) -> new BaseButton(x, y, w, h, "кынопка 3", b -> {
                        }, new ResourceLocation(MODID, "textures/gui/buttons/hollow_button.png")),
                        Alignment.LEFT_CENTER, 20, 20, this.width, this.height, 90, 20)
        );
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        super.render(stack, mouseX, mouseY, ticks);

        bind(MODID, "gui/background_ftbq.png");
        betterBlit(stack, Alignment.TOP_CENTER, 0, -10, 200, 100);

        drawString(stack, "любой текст", Alignment.TOP_CENTER, 0, 0, 0xFFFFFF);
    }


}
