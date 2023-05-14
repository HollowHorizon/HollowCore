package ru.hollowhorizon.hc.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.client.math.Spline3D;
import ru.hollowhorizon.hc.client.screens.widget.SliderWidget;
import ru.hollowhorizon.hc.common.animations.CameraPath;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CameraScreen extends Screen {
    //private final List<CameraPath> allPoints = new ArrayList<>();
    private Spline3D currentPath;
    private int currentFrame = 0;
    private int interpolationId = 0;
    private Vector3d lastPos;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private final List<Vector3d> points = new ArrayList<>();

    public CameraScreen() {
        super(new StringTextComponent("CAMERA_SCREEN"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(0, 0, 80, 20, new StringTextComponent("Add Point"), (button) -> {
            Vector3d currentPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            this.points.add(currentPos);
        }));
        this.addButton(new Button(0, 20, 80, 20, new StringTextComponent("Change Path"), (button) -> {
            currentPath = new Spline3D(this.points);
        }));

        SliderWidget slider = new SliderWidget(this.width - 64, 0, 64, 16);
        slider.setMin(0);
        slider.setMax(99);
        slider.setValueConsumer(value -> currentFrame = value.intValue());
        this.addButton(slider);

        SliderWidget sliderX = new SliderWidget(this.width - 64, 16, 64, 16);
        sliderX.setMode(2);
        sliderX.setValue(Minecraft.getInstance().player.getX());
        sliderX.setValueConsumer(value -> this.x = value);
        this.addButton(sliderX);

        SliderWidget sliderY = new SliderWidget(this.width - 64, 32, 64, 16);
        sliderY.setMode(2);
        sliderY.setValue(Minecraft.getInstance().player.getY());
        sliderY.setValueConsumer(value -> this.y = value);
        this.addButton(sliderY);

        SliderWidget sliderZ = new SliderWidget(this.width - 64, 48, 64, 16);
        sliderZ.setMode(2);
        sliderZ.setValue(Minecraft.getInstance().player.getZ());
        sliderZ.setValueConsumer(value -> this.z = value);
        this.addButton(sliderZ);

    }

    @SubscribeEvent
    public void worldRender(RenderWorldLastEvent event) {
        event.getMatrixStack().pushPose();


        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        event.getMatrixStack().translate(-projectedView.x, -projectedView.y, -projectedView.z);

        if(currentPath!=null) currentPath.draw(event.getMatrixStack());

        event.getMatrixStack().popPose();
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        event.setYaw(yaw);
        event.setPitch(pitch);

        ActiveRenderInfo camera = event.getInfo();

        if (currentPath != null) camera.setPosition(currentPath.getPoint(currentFrame / 100.0));
        else {
            camera.setPosition(this.x, this.y, this.z);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int scan, double dragX, double dragY) {
        if (scan == 2) {
            this.yaw += dragX;
            this.pitch += dragY;
        }
        return super.mouseDragged(p_231045_1_, p_231045_3_, scan, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
