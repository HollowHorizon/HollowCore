package ru.hollowhorizon.hc.client.screens.widget.list;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.hollowhorizon.hc.client.screens.util.WidgetPlacement;
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget;
import ru.hollowhorizon.hc.client.screens.widget.VerticalSliderWidget;
import ru.hollowhorizon.hc.client.screens.widget.WidgetBox;
import ru.hollowhorizon.hc.client.utils.ScissorUtil;
import ru.hollowhorizon.hc.common.ui.Alignment;

import java.util.List;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.toSTC;

public class ListWidget extends HollowWidget {
    private final List<AbstractWidget> listWidgets;
    protected boolean autoSize = false;
    private VerticalSliderWidget slider;
    private int maxHeight;
    private int currentHeight = 0;
    private boolean isSliderInit = false;

    public ListWidget(List<AbstractWidget> widgets) {
        this(widgets, 0, 0, 0, 0);
    }

    public ListWidget(List<AbstractWidget> widgets, int x, int y, int width, int height) {
        super(x, y, width, height, toSTC("LIST_WIDGET"));

        this.listWidgets = widgets;

        init();
    }

    @Override
    public void playDownSound(SoundManager p_230988_1_) {
    }

    @Override
    public void init() {
        initSlider();

        int y = 5;
        for (AbstractWidget widget : listWidgets) {
            y += widget.getHeight() + 5;
        }
        y -= this.height;
        maxHeight = y;

        this.widgets.clear();

        y = this.y + 5 - currentHeight;
        for (AbstractWidget widget : listWidgets) {
            if (widget instanceof WidgetBox box) {

                box.setX(this.x + (this.width - 10) / 2 - widget.getWidth() / 2);
                box.setY(y);
                y += 5 + widget.getHeight();
                continue;
            }

            if (!autoSize) {
                if (widget instanceof HollowWidget) {
                    ((HollowWidget) widget).setX(this.x + (this.width - 10) / 2 - widget.getWidth() / 2);
                    ((HollowWidget) widget).setY(y);
                } else {
                    widget.x = this.x + (this.width - 10) / 2 - widget.getWidth() / 2;
                    widget.y = y;
                }
                y += 5 + widget.getHeight();
            }
        }

        this.addWidgets(listWidgets.toArray(new AbstractWidget[0]));
    }

    private void initSlider() {
        if (!isSliderInit) {
            isSliderInit = true;
            this.slider = WidgetPlacement.configureWidget(VerticalSliderWidget::new, Alignment.TOP_RIGHT, this.x, -this.y, this.width, this.height, 10, this.height);
            this.slider.onValueChange(f -> {
                if (maxHeight > 0) {
                    this.currentHeight = (int) (maxHeight * f);
                    init();
                }
            });
        }
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float ticks) {
        this.slider.render(stack, mouseX, mouseY, ticks);

        ScissorUtil.INSTANCE.push(this.x, this.y, this.width, this.height);
        super.renderButton(stack, mouseX, mouseY, ticks);
        ScissorUtil.INSTANCE.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.slider.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.slider.mouseReleased(mouseX, mouseY, button);
        if (isHovered) {
            return super.mouseReleased(mouseX, mouseY, button);
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (maxHeight > 0) {
            currentHeight -= scroll * 5;
            currentHeight = Mth.clamp(currentHeight, 0, maxHeight);
            if (this.slider != null) {
                this.slider.setScroll(currentHeight / (maxHeight + 0.0F));
            }
        }

        init();
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
}
