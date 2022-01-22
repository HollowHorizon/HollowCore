package ru.hollowhorizon.hc.client.utils.tools;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModRenderTypes extends RenderType {
    private static final RenderType ARMOR_ENTITY_GLINT = create(MODID + ":armor_entity_glint", DefaultVertexFormats.POSITION_TEX, GL11.GL_TRIANGLES, 256,
            RenderType.State.builder()
                    .setTextureState(new RenderState.TextureState(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(RenderState.NO_CULL)
                    .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(ENTITY_GLINT_TEXTURING)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)
    );

    public ModRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn,
                          boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getAnimatedModel(ResourceLocation locationIn) {
        RenderType.State state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(locationIn, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(RenderState.NO_CULL)
                .setLightmapState(RenderState.LIGHTMAP)
                .setOverlayState(RenderState.OVERLAY)
                .createCompositeState(true);

        return create(MODID + ":animated_model2", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256, true, false, state);
    }

    public static RenderType getItemEntityTranslucentCull(ResourceLocation locationIn) {
        RenderType.State rendertype$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(locationIn, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setLightmapState(RenderState.LIGHTMAP)
                .setOverlayState(RenderState.OVERLAY)
                .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                .createCompositeState(true);

        return create(MODID + ":item_entity_translucent_cull", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256, true, false, rendertype$state);
    }

    public static RenderType getAimHelper() {
        RenderType.State rendertype$state = RenderType.State.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(RenderState.NO_DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setLightmapState(RenderState.NO_LIGHTMAP)
                .setOverlayState(RenderState.NO_OVERLAY)
                .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                .createCompositeState(true);

        return create(MODID + ":aim_helper", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, true, false, rendertype$state);
    }

    public static RenderType getAnimatedArmorModel(ResourceLocation locationIn) {
        RenderType.State state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(locationIn, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(RenderState.NO_CULL)
                .setLightmapState(RenderState.LIGHTMAP)
                .setOverlayState(RenderState.OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);

        return create(MODID + ":animated_armor_model", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256, true, false, state);
    }

    public static RenderType getEntityCutoutNoCull(ResourceLocation locationIn) {
        RenderType.State state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(locationIn, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(RenderState.CULL)
                .setLightmapState(RenderState.LIGHTMAP)
                .setOverlayState(RenderState.OVERLAY)
                .createCompositeState(true);

        return create(MODID + ":entity_cutout_no_cull", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256, true, false, state);
    }

    public static RenderType getEntityIndicator(ResourceLocation locationIn) {
        RenderType.State state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(locationIn, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false);

        return create(MODID + ":entity_indicator", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256, false, false, state);
    }

    public static RenderType getBox() {
        RenderType.State state = RenderType.State.builder()
                .setTransparencyState(NO_TRANSPARENCY)
                .setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false);

        return create(MODID + ":box", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256, false, false, state);
    }

    public static RenderType getLine() {
        RenderType.State state = RenderType.State.builder()
                .setTransparencyState(NO_TRANSPARENCY)
                .setAlphaState(DEFAULT_ALPHA)
                .createCompositeState(false);

        return create(MODID + ":line", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, false, false, state);
    }

    public static RenderType getEnchantedArmor() {
        return ARMOR_ENTITY_GLINT;
    }

    public static IVertexBuilder getArmorVertexBuilder(IRenderTypeBuffer buffer, RenderType renderType, boolean withGlint) {
        return withGlint ? VertexBuilderUtils.create(buffer.getBuffer(getEnchantedArmor()), buffer.getBuffer(renderType))
                : buffer.getBuffer(renderType);
    }
}
