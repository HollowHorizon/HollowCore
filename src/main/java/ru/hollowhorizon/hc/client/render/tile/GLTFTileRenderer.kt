package ru.hollowhorizon.hc.client.render.tile


import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import ru.hollowhorizon.hc.client.gltf.IAnimated

class GLTFTileRenderer<T>(val dispatcher: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<T> where T : BlockEntity, T : IAnimated {


    fun preRender(tile: T) {
//        //val manager = tile.getManager()
//        var shouldUpdate = false
//        manager.animations.removeIf { animation ->
//            val doRemove = allAnimations?.find { anim -> anim.name == animation.name }
//                ?.update(animation.tick(), animation.loop) ?: false
//            shouldUpdate = shouldUpdate || doRemove
//            return@removeIf doRemove
//        }
//        if (shouldUpdate) {
//          /  tile.setManager(manager)
//        }
    }

    override fun render(
        tile: T,
        particalTick: Float,
        stack: PoseStack,
        p_225616_4_: MultiBufferSource,
        p_225616_5_: Int,
        p_225616_6_: Int,
    ) {
        preRender(tile)

//        GL11.glPushMatrix()
//        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
//        GL11.glEnable(GL11.GL_LIGHTING)
//        GL11.glShadeModel(GL11.GL_SMOOTH)
//        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
//        GL11.glEnable(GL11.GL_DEPTH_TEST)
//        GL11.glEnable(GL11.GL_COLOR_MATERIAL)
//        GL11.glEnable(GL11.GL_ALPHA_TEST)
//        RenderSystem.enableBlend()
//        RenderSystem.blendFuncSeparate(
//            GlStateManager.SourceFactor.SRC_ALPHA,
//            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
//            GlStateManager.SourceFactor.ONE,
//            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
//        )
//
//        GL11.glAlphaFunc(516, 0.1f)
//
//        RenderSystem.multMatrix(p_225623_4_.last().pose())
//
//        if (GlTFModelManager.getInstance().isShaderModActive) {
//            renderedScene!!.renderForShaderMod()
//        } else {
//            GL13.glActiveTexture(GL13.GL_TEXTURE2)
//            GL11.glEnable(GL11.GL_TEXTURE_2D)
//            GL13.glActiveTexture(GL13.GL_TEXTURE0)
//            renderedScene!!.renderForVanilla()
//        }
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
//        GL30.glBindVertexArray(0)
//        RenderedGltfModel.nodeGlobalTransformLookup.clear()
//        GL11.glPopAttrib()
//        GL11.glPopMatrix()
//        super.render(tile, particalTick, stack, p_225616_4_, p_225616_5_, p_225616_6_)
    }
}