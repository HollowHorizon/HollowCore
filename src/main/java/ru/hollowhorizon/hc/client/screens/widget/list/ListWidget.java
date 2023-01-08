package ru.hollowhorizon.hc.client.screens.widget.list;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import ru.hollowhorizon.hc.client.screens.util.Alignment;
import ru.hollowhorizon.hc.client.screens.util.WidgetPlacement;
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget;
import ru.hollowhorizon.hc.client.screens.widget.VerticalSliderWidget;
import ru.hollowhorizon.hc.client.screens.widget.WidgetBox;
import ru.hollowhorizon.hc.client.utils.ScissorUtil;

import java.util.List;

public class ListWidget extends HollowWidget {
    private final List<Widget> listWidgets;
    protected boolean autoSize = false;
    private VerticalSliderWidget slider;
    private int maxHeight;
    private int currentHeight = 0;
    private boolean isSliderInit = false;

    public ListWidget(List<Widget> widgets) {
        this(widgets, 0, 0, 0, 0);
    }

    public ListWidget(List<Widget> widgets, int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent("LIST_WIDGET"));

        this.listWidgets = widgets;

        init();
    }

    @Override
    public void playDownSound(SoundHandler p_230988_1_) {
    }

    @Override
    public void init() {
        initSlider();

        int y = 5;
        for (Widget widget : listWidgets) {
            y += widget.getHeight() + 5;
        }
        y -= this.height;
        maxHeight = y;

        this.getWidgets().clear();

        y = this.y + 5 - currentHeight;
        for (Widget widget : listWidgets) {
            if (widget instanceof WidgetBox) {
                WidgetBox box = (WidgetBox) widget;

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

        this.addWidgets(listWidgets.toArray(new Widget[0]));
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
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        this.slider.render(stack, mouseX, mouseY, ticks);

        ScissorUtil.start(this.x, this.y, this.width, this.height);
        super.renderButton(stack, mouseX, mouseY, ticks);
        ScissorUtil.stop();
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
            currentHeight = MathHelper.clamp(currentHeight, 0, maxHeight);
            if (this.slider != null) {
                this.slider.setScroll(currentHeight / (maxHeight + 0.0F));
            }
        }

        init();
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
}
