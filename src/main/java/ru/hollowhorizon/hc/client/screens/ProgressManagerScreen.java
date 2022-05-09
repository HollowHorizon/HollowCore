package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import ru.hollowhorizon.hc.client.screens.widget.button.SaveButton;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.LoadProgressToServer;
import ru.hollowhorizon.hc.common.network.messages.SaveProgressToServer;

import java.util.List;

public class ProgressManagerScreen extends Screen {
    private final ITextComponent saveMessage = new TranslationTextComponent("hollowcore.gui.save_handler.message_1");
    List<ITextComponent> saveTime;

    public static void open(List<ITextComponent> saveTime) {
        Minecraft.getInstance().setScreen(new ProgressManagerScreen(saveTime));
    }

    public ProgressManagerScreen(List<ITextComponent> saveTime) {
        super(new StringTextComponent("SAVE_PROGRESS_SCREEN"));
        this.saveTime = saveTime;
    }

    @Override
    protected void init() {

        this.addButton(
                new SaveButton(
                        this.width / 2 - 50,
                        this.height / 8,
                        200,
                        20,
                        new TranslationTextComponent("hc.gui.save_handler").append(" 1: ").append(saveTime.get(0)),
                        (button) -> Minecraft.getInstance().setScreen(new SaveOrLoad(0))
                )
        );
        this.addButton(
                new SaveButton(
                        this.width / 2 - 50,
                        this.height / 8 + 40,
                        200,
                        20,
                        new TranslationTextComponent("hc.gui.save_handler").append(" 2: ").append(saveTime.get(1)),
                        (button) -> Minecraft.getInstance().setScreen(new SaveOrLoad(1))
                )
        );
        this.addButton(
                new SaveButton(
                        this.width / 2 - 50,
                        this.height / 8 + 80,
                        200,
                        20,
                        new TranslationTextComponent("hc.gui.save_handler").append(" 3: ").append(saveTime.get(2)),
                        (button) -> Minecraft.getInstance().setScreen(new SaveOrLoad(2))
                )
        );
        this.addButton(
                new SaveButton(
                        this.width / 2 - 50,
                        this.height / 8 + 120,
                        200,
                        20,
                        new TranslationTextComponent("hc.gui.save_handler").append(" 4: ").append(saveTime.get(3)),
                        (button) -> Minecraft.getInstance().setScreen(new SaveOrLoad(3))
                )
        );
        this.addButton(
                new SaveButton(
                        this.width / 2 - 50,
                        this.height / 8 + 160,
                        200,
                        20,
                        new TranslationTextComponent("hc.gui.save_handler").append(" 5: ").append(saveTime.get(4)),
                        (button) -> Minecraft.getInstance().setScreen(new SaveOrLoad(4))
                )
        );
    }

    @Override
    public void render(MatrixStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);

        Minecraft.getInstance().font.drawShadow(stack, saveMessage, this.width / 2F - Minecraft.getInstance().font.width(saveMessage) / 2F, this.height / 8F - 20, 0xFFFFFF);
    }

    public static class SaveOrLoad extends Screen {
        private final ITextComponent choiceMessage = new TranslationTextComponent("hollowcore.gui.save_handler.message_2");
        private final int slot;

        protected SaveOrLoad(int slot) {
            super(new StringTextComponent("SAVE_OR_LOAD"));
            this.slot = slot;
        }

        @Override
        protected void init() {
            this.addButton(
                    new Button(
                            this.width / 2 - 50,
                            this.height / 3,
                            200,
                            20,
                            new TranslationTextComponent("hc.gui.save_handler.save"),
                            (button) -> NetworkHandler.sendMessageToServer(new SaveProgressToServer(slot))
                    )
            );
            this.addButton(
                    new Button(
                            this.width / 2 - 50,
                            this.height / 3 + 40,
                            200,
                            20,
                            new TranslationTextComponent("hc.gui.save_handler.load"),

                            (button) -> NetworkHandler.sendMessageToServer(new LoadProgressToServer(slot))
                    )
            );
        }

        @Override
        public void render(MatrixStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
            renderBackground(stack);

            Minecraft.getInstance().font.drawShadow(stack, choiceMessage, this.width / 2F - Minecraft.getInstance().font.width(choiceMessage) / 2F, this.height / 3F - 20, 0xFFFFFF);
            super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
        }
    }
}
