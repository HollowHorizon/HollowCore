package ru.hollowhorizon.hc.client.render.tile

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.LivingEntity
import net.minecraft.tileentity.TileEntity
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity

class GLTFTileRenderer<T>(dispatcher: TileEntityRendererDispatcher) :
    TileEntityRenderer<T>(dispatcher) where T : TileEntity, T : IAnimatedEntity {


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
        stack: MatrixStack,
        p_225616_4_: IRenderTypeBuffer,
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