package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import ru.hollowhorizon.hc.client.screens.widget.VolumeWidget;
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton;
import ru.hollowhorizon.hc.client.screens.widget.button.ChoiceButton;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.TextHelper;
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.DialogueEndToServer;
import ru.hollowhorizon.hc.common.story.dialogues.ChoiceTextComponent;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueIterator;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class DialogueScreen extends Screen {
    public final ResourceLocation OPTIONS = new ResourceLocation(MODID, "textures/gui/icons/options.png");
    public final ResourceLocation PANEL = new ResourceLocation(MODID, "textures/gui/save_obelisk_block.png");
    public final VolumeWidget volumeWidget = new VolumeWidget(10, 0, 40, 10);
    private final int guiScale;
    private final String dialogueName;
    public DialogueIterator iterator;
    public String text;
    public String nextText;
    public ITextComponent characterName = new StringTextComponent("???");
    public ResourceLocation BG;
    public int dialogueTicks;
    public int stringTicks = 0;
    public int overlayAnimationTicks = 0;
    public int stringAnimationTicks = 0;
    public boolean isButtonsCreated = false;
    public boolean isLineEnded = true;
    public Entity[] CHARACTERS;
    private ResourceLocation LAST_BG;
    private boolean hasSkipButton;
    private Consumer<DialogueScreen> action;
    private int bgCounter;

    public DialogueScreen(HollowDialogue dialogue) {
        super(new StringTextComponent("DIALOGUE_SCREEN"));
        this.guiScale = Minecraft.getInstance().options.guiScale;
        Minecraft.getInstance().options.guiScale = 4;
        Minecraft.getInstance().options.hideGui = true;
        Minecraft.getInstance().resizeDisplay();
        this.dialogueName = GUIDialogueHandler.getRegName(dialogue);
        this.iterator = dialogue.iterator();
        updateStrings();
    }

    public static void openGUI(HollowDialogue dialogue) {
        Minecraft.getInstance().setScreen(new DialogueScreen(dialogue));
    }

    public static void drawStringNoShadow(MatrixStack stack, FontRenderer fr, ITextComponent text, int x, int y, int color, int alpha) {
        stack.pushPose();
        stack.translate(0.0D, 0.0D, 200.0D);


        GL14.glBlendFuncSeparate(770, 771, 1, 0);
        GL11.glAlphaFunc(516, 0.1F);

        fr.draw(stack, text.getString(), (float) (x), (float) y, color | (alpha << 24));

        stack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void updateStrings() {
        if ((text == null || stringTicks == text.length()) && !iterator.isChoiceNow()) {
            if (!isLineEnded) return;
            else if (!iterator.hasNext()) {
                onClose();
                return;
            }

            isButtonsCreated = false;
            isLineEnded = false;

            iterator.processDialogueComponent(
                    (textComponent) -> {
                        ITextComponent dialogueText = textComponent.getText();
                        ITextComponent dialogueCharacterName = textComponent.getCharacterName();
                        ResourceLocation dialogueBG = textComponent.getBG();
                        Entity[] entities = textComponent.getCharacters();
                        Consumer<DialogueScreen> action = textComponent.getAction();
                        String sound = textComponent.getAudio();

                        //???????????????????????? ??????????
                        if (dialogueText != null) {
                            assert Minecraft.getInstance().player != null;
                            if (text == null) {
                                text = dialogueText.getString().replaceAll("%PLAYER%", Minecraft.getInstance().player.getGameProfile().getName());
                            } else
                                nextText = dialogueText.getString().replaceAll("%PLAYER%", Minecraft.getInstance().player.getGameProfile().getName());
                        }

                        //???????????????????????? ?????? ??????????????????
                        if (dialogueCharacterName != null) {
                            if (dialogueCharacterName.getString().equals("%PLAYER%") && Minecraft.getInstance().player != null) {
                                characterName = new StringTextComponent(Minecraft.getInstance().player.getGameProfile().getName());
                            } else if (dialogueCharacterName.getString().equals("%NULL%")) {
                                characterName = new StringTextComponent("");
                            } else {
                                characterName = dialogueCharacterName.copy().append(": ");
                            }
                        } else {
                            characterName = new StringTextComponent("");
                        }

                        //???????????????????????? ???????????? ??????
                        LAST_BG = BG;
                        BG = dialogueBG;


                        //???????????????????????? ??????????
                        if (entities != null) {
                            CHARACTERS = entities;
                        }

                        //???????????????????????? ????????????????
                        if (action != null) {
                            this.action = action;
                        }

                        //???????????????????????? ??????????
                        if (sound != null) {
                            HollowJavaUtils.nothing();
                        }

                        hasSkipButton = textComponent.hasSkipButton();

                        dialogueTicks = textComponent.getAutoSkip();

                        init();
                    },
                    (choiceComponent) -> {

                    },
                    (effectComponent) -> {
                    }
            );


        } else if (stringTicks != text.length()) {
            stringTicks = text.length();
        }
    }

    @Override
    public void tick() {
        if (dialogueTicks == -2) {
            if (stringTicks == text.length()) {
                updateStrings();
            }
        } else if (dialogueTicks > -1) {
            if (dialogueTicks > 0) dialogueTicks--;
            else {
                if (stringTicks == text.length()) {
                    updateStrings();
                }
            }
        }
    }

    @Override
    protected void init() {
        this.buttons.clear();
        this.children.clear();
        super.init();

        iterator.prepareChoices((choiceComponent) -> {
            ITextComponent[] texts = choiceComponent.getTexts();
            int size = texts.length;
            for (int i = 0; i < size; i++) {
                ITextComponent label = texts[i];
                this.addButton(new ChoiceButton(this.width / 8, 20 + i * 25, this.width - this.width / 4, 20, label, (button) -> {
                    this.buttons.clear();
                    this.children.clear();

                    isLineEnded = false;
                    isButtonsCreated = false;

                    iterator.makeChoice(choiceComponent, (ChoiceTextComponent) button.getMessage(), dialogueName);

                    updateStrings();
                }));
            }
        });

        this.addButton(new BaseButton(0, 0, 10, 10, "", (button) -> Minecraft.getInstance().setScreen(new DialogueOptionsScreen(this)), OPTIONS));

        this.addButton(
                volumeWidget
        );

        if (hasSkipButton) {
            this.addButton(new ChoiceButton((int) (this.width - this.width / 5.5F), this.height - this.height / 7, this.width / 6, this.height / 12, new TranslationTextComponent("hollowcore.gui.skip"), (button) -> onClose()));
        }
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        if (overlayAnimationTicks > 14) {
            drawBG(stack);
            drawCharacters(mouseX, mouseY);
            drawOverlay(stack, 15);
            drawAnimatedStrings(stack);
        } else {
            drawOverlay(stack, overlayAnimationTicks);
            overlayAnimationTicks++;
        }

        drawOptionsPanel(stack);

        super.render(stack, mouseX, mouseY, p_230430_4_);

    }

    public void drawOptionsPanel(MatrixStack stack) {
        stack.pushPose();
        stack.translate(0.0D, 0.0D, -1000.0D);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        Minecraft.getInstance().getTextureManager().bind(PANEL);
        blit(stack, 0, 0, 0, 0, this.width, 10, this.width, 10);
        stack.popPose();

        Calendar calendar = new GregorianCalendar();
        String time = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        drawStringNoShadow(stack, Minecraft.getInstance().font, time, (int) (this.width - Minecraft.getInstance().font.width(time)), 1, 0xFF);
    }

    public void drawOverlay(MatrixStack stack, int offsetX) {
        stack.pushPose();
        stack.translate(0.0D, 0.0D, -1000.0D);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        Minecraft.getInstance().getTextureManager().bind(new ResourceLocation(MODID, "textures/gui/lore/mess.png"));
        blit(stack, this.width / 2 - (this.width / 2) / 15 * offsetX, (int) (this.height - this.height / 5F), 0, 0, this.width / 15 * offsetX, (int) (this.height / 3.5), this.width / 15 * offsetX, (int) (this.height / 3.5));
        stack.popPose();

    }

    public void drawCharacters(float rotationX, float rotationY) {
        if (CHARACTERS == null || CHARACTERS.length == 0) return;

        int size = CHARACTERS.length;

        for (int i = 0; i < size; i++) {
            if (CHARACTERS[i] instanceof LivingEntity) {
                Entity entity = CHARACTERS[i];
                float scaleFactor = entity.getBbHeight() / 1.8F;
                scaleFactor = Math.max(scaleFactor, 0.5F);
                float adaptiveScale = (int) (100.0F / scaleFactor);

                int xDistance = this.width / (CHARACTERS.length + 1);

                if (action != null) {
                    action.accept(this);
                }

                InventoryScreen.renderEntityInInventory(xDistance * (i + 1), this.height, (int) adaptiveScale, (float) xDistance * (i + 1) - rotationX, this.height / 2F - rotationY, (LivingEntity) entity);
            }
        }
    }

    public Entity[] getCharacters() {
        return CHARACTERS;
    }

    public void drawBG(MatrixStack matrixStack) {
        if (BG != null) {
            matrixStack.pushPose();
            matrixStack.translate(0.0D, 0.0D, -1000.0D);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            Minecraft.getInstance().getTextureManager().bind(BG);
            blit(matrixStack, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            matrixStack.popPose();
        } else {
            if (LAST_BG != null) {
                if (bgCounter < 15) {
                    matrixStack.pushPose();
                    matrixStack.translate(0.0D, 0.0D, -1000.0D);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.defaultAlphaFunc();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, bgCounter / 14F);
                    Minecraft.getInstance().getTextureManager().bind(LAST_BG);
                    blit(matrixStack, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
                    matrixStack.popPose();
                    bgCounter++;
                } else {
                    LAST_BG = null;
                    bgCounter = 0;
                }
            }
        }
    }

    public void drawAnimatedStrings(MatrixStack stack) {
        drawStringNoShadow(stack, Minecraft.getInstance().font, characterName, (int) (this.width / 4 - Minecraft.getInstance().font.width(characterName.getString())), this.height - this.height / 6, 0xFFFFFF, 0xFF);

        if (text.length() > stringTicks) stringTicks++;
        else {
            if (nextText != null) {
                stringAnimationTicks++;
            }
        }

        List<String> strings = TextHelper.splitString(text.substring(0, stringTicks));
        for (int i = 0; i < strings.size(); i++) {

            if (stringAnimationTicks <= (this.height / 24) * (i + 1)) {

                drawStringNoShadow(stack, Minecraft.getInstance().font, strings.get(i), this.width / 4, this.height - this.height / 6 + (this.height / 24) * i - stringAnimationTicks, 0xFF - stringAnimationTicks / (i + 1) * 22);

            } else if (i == strings.size() - 1) {

                stringTicks = 0;
                stringAnimationTicks = 0;
                text = nextText;
                nextText = null;
                return;
            }
        }

        if (text.length() == stringTicks && nextText == null) {
            isLineEnded = true;
            if (!isButtonsCreated) createButtons();
        }
    }

    protected void drawStringNoShadow(MatrixStack stack, FontRenderer fr, String text, int x, int y, int alpha) {
        drawStringNoShadow(stack, fr, new StringTextComponent(text), x, y, 0xFFFFFF, alpha);
    }

    public void createButtons() {
        if (iterator.hasNext() && iterator.isChoiceNow()) {
            isButtonsCreated = true;
            init();
        }
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        if (volumeWidget.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_)) return true;
        return super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
    }

    public boolean keyPressed(int key, int scan, int param3) {
        if (super.keyPressed(key, scan, param3)) {
            return true;
        } else {

            if (this.isExitKey(key, scan)) {
                updateStrings();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0) {
            updateStrings();
            return true;
        }

        return false;
    }

    protected boolean isExitKey(int key, int scan) {
        return key == 256 || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputMappings.getKey(key, scan)) || Minecraft.getInstance().options.keyJump.isActiveAndMatches(InputMappings.getKey(key, scan));
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().options.guiScale = guiScale;
        Minecraft.getInstance().options.hideGui = false;
        Minecraft.getInstance().resizeDisplay();

        NetworkHandler.sendMessageToServer(new DialogueEndToServer(dialogueName));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
