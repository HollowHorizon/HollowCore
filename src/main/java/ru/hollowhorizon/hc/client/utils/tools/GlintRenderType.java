package ru.hollowhorizon.hc.client.utils.tools;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static ru.hollowhorizon.hc.HollowCore.MODID;

@OnlyIn(Dist.CLIENT)
public class GlintRenderType {

    public static List<RenderType> glintColor = newRenderList(GlintRenderType::buildGlintRenderType);
    public static List<RenderType> entityGlintColor = newRenderList(GlintRenderType::buildEntityGlintRenderType);
    public static List<RenderType> glintDirectColor = newRenderList(GlintRenderType::buildGlintDirectRenderType);
    public static List<RenderType> entityGlintDirectColor = newRenderList(GlintRenderType::buildEntityGlintDirectRenderType);

    public static List<RenderType> armorGlintColor = newRenderList(GlintRenderType::buildArmorGlintRenderType);
    public static List<RenderType> armorEntityGlintColor = newRenderList(GlintRenderType::buildArmorEntityGlintRenderType);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map) {
        addGlintTypes(map, glintColor);
        addGlintTypes(map, entityGlintColor);
        addGlintTypes(map, glintDirectColor);
        addGlintTypes(map, entityGlintDirectColor);
        addGlintTypes(map, armorGlintColor);
        addGlintTypes(map, armorEntityGlintColor);
    }

    private static List<RenderType> newRenderList(Function<String, RenderType> func) {
        ArrayList<RenderType> list = new ArrayList<>(17);

        for (DyeColor color : DyeColor.values())
            list.add(func.apply(color.getName()));
        list.add(func.apply("rainbow"));

        return list;
    }

    private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, List<RenderType> typeList) {
        for(RenderType renderType : typeList)
            if (!map.containsKey(renderType))
                map.put(renderType, new BufferBuilder(renderType.bufferSize()));
    }

    private static RenderType buildGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("glint_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setOutputState(RenderState.ITEM_ENTITY_TARGET)
                .setTexturingState(RenderState.GLINT_TEXTURING)
                .createCompositeState(false));
    }

    private static RenderType buildEntityGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("entity_glint_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setOutputState(RenderState.ITEM_ENTITY_TARGET)
                .setTexturingState(RenderState.ENTITY_GLINT_TEXTURING)
                .createCompositeState(false));
    }


    private static RenderType buildGlintDirectRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setTexturingState(RenderState.GLINT_TEXTURING)
                .createCompositeState(false));
    }


    private static RenderType buildEntityGlintDirectRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setTexturingState(RenderState.ENTITY_GLINT_TEXTURING)
                .createCompositeState(false));
    }

    private static RenderType buildArmorGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setTexturingState(RenderState.ENTITY_GLINT_TEXTURING)
                .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(false));
    }

    private static RenderType buildArmorEntityGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(MODID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.create("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(res, true, false))
                .setWriteMaskState(RenderState.COLOR_WRITE)
                .setCullState(RenderState.NO_CULL)
                .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                .setTransparencyState(RenderState.GLINT_TRANSPARENCY)
                .setTexturingState(RenderState.ENTITY_GLINT_TEXTURING)
                .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(false));
    }
}
