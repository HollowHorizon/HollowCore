package ru.hollowhorizon.hc.client.render.entity

import com.modularmods.mcgltf.IGltfModelReceiver
import com.modularmods.mcgltf.MCglTF
import com.modularmods.mcgltf.RenderedGltfModel
import com.modularmods.mcgltf.RenderedGltfScene
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import net.minecraftforge.fml.ModLoader
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import kotlin.jvm.optionals.getOrNull


class GLTFEntityRenderer<T>(manager: EntityRendererProvider.Context) :
    EntityRenderer<T>(manager), IGltfModelReceiver where T : LivingEntity/*, T : IAnimatedEntity*/ {

    init {
        MCglTF.getInstance().addGltfModelReceiver(this)
    }

    private lateinit var templates: HashMap<AnimationType, String>
    private var manager: AnimationManager? = null
    private var renderedScene: RenderedGltfScene? = null

    override fun onReceiveSharedModel(renderedModel: RenderedGltfModel) {
        renderedScene = renderedModel.renderedGltfScenes[0]
        this.manager = AnimationManager(renderedModel)
        this.templates =
            AnimatedEntityCapability().apply { AnimationType.load(renderedModel.gltfModel, this) }.animations
    }

    override fun getTextureLocation(entity: T): ResourceLocation {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        yaw: Float,
        partialTick: Float,
        stack: PoseStack,
        p_225623_5_: MultiBufferSource,
        packedLight: Int,
    ) {
        if (renderedScene == null || manager == null) return

        val type = getRenderType(entity)

        stack.pushPose()


        preRender(entity, stack, partialTick)

        val currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)
        val currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)
        val currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        stack.pushPose()

        val lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        stack.mulPose(Vector3f.YP.rotationDegrees(-lerpBodyRot))
        stack.scale(1.5f, 1.5f, 1.5f)

        RenderedGltfModel.CURRENT_POSE = stack.last().pose()
        RenderedGltfModel.CURRENT_NORMAL = stack.last().normal()
        stack.popPose()

        GL30.glVertexAttribI2i(
            RenderedGltfModel.vaUV2,
            packedLight and '\uffff'.code,
            packedLight shr 16 and '\uffff'.code
        )

        GL30.glVertexAttribI2i(
            RenderedGltfModel.vaUV1,
            (partialTick * 15).toInt(),
            if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10
        )

        if (MCglTF.getInstance().isShaderModActive) {
            type.setupRenderState()
            renderedScene?.renderForShaderMod()
            type.clearRenderState()
        } else {
            type.setupRenderState()

            GL13.glActiveTexture(GL13.GL_TEXTURE2) //Лайтмап
            val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)
            GL13.glActiveTexture(GL13.GL_TEXTURE1) //Оверлей
            val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            val overlay = if (entity.hurtTime > 0 || !entity.isAlive) RenderSystem.getShaderTexture(1) else 0
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlay)
            GL13.glActiveTexture(GL13.GL_TEXTURE0) //Текстуры модели
            val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

            renderedScene?.renderForVanilla()

            GL13.glActiveTexture(GL13.GL_TEXTURE2) //Возврат Лайтмапа
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
            GL13.glActiveTexture(GL13.GL_TEXTURE1) //Возврат Оверлея
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
            GL13.glActiveTexture(GL13.GL_TEXTURE0) //Возврат Исходных текстур
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
            type.clearRenderState()

            if(Minecraft.getInstance().shouldEntityAppearGlowing(entity)) {
                val outline = type.outline().getOrNull() ?: return
                outline.setupRenderState()

                GL13.glActiveTexture(GL13.GL_TEXTURE2) //Лайтмап
                val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)
                GL30.glVertexAttribI2i(
                    RenderedGltfModel.vaUV2,
                    255,
                    255
                )
                GL13.glActiveTexture(GL13.GL_TEXTURE1) //Оверлей
                val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1))
                GL13.glActiveTexture(GL13.GL_TEXTURE0) //Текстуры модели
                val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().defaultColorMap)
                GL13.glActiveTexture(GL13.GL_TEXTURE10) //Текстуры модели

                renderedScene?.renderForVanilla()

                GL13.glActiveTexture(GL13.GL_TEXTURE2)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
                GL13.glActiveTexture(GL13.GL_TEXTURE1)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)

                outline.clearRenderState()
            }
        }

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0)

        GL30.glBindVertexArray(currentVAO)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        stack.popPose()

        super.render(entity, yaw, partialTick, stack, p_225623_5_, packedLight)
    }

    protected fun getRenderType(
        entity: T,
    ): RenderType {
        val location = getTextureLocation(entity)
        return if (!entity.isInvisible && entity.isInvisibleTo(Minecraft.getInstance().player!!)) {
            RenderType.itemEntityTranslucentCull(location)
        } else RenderType.entityTranslucent(location)
    }


    private fun preRender(entity: T, stack: PoseStack, partialTick: Float) {
        if (manager == null) return
        updateAnimations(entity, manager!!)
        manager!!.update(partialTick)
    }

    private fun updateAnimations(entity: T, manager: AnimationManager) {
        if (!entity.isAlive) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.DEATH, "")
            return
        }

        if (entity is FlyingAnimal) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.FLY, "")
            return
        }

        if (entity.isSleeping) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.SLEEP, "")
            return
        }

        if (entity.swinging) {
//            val anim = AnimationLoader.createAnimation(
//                renderedScene.gl!!.model?.gltfModel ?: return,
//                templates.getOrDefault(AnimationType.SWING, "")
//            ) ?: return
            //manager.addLayer(anim)
            return
        }

        entity.vehicle?.let {
            manager.currentAnimation = templates.getOrDefault(AnimationType.SIT, "")
            return
        }

        if (entity.fallFlyingTicks > 4) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.FALL, "")
            return
        }

        manager.currentAnimation = if (entity.animationSpeed > 0.01) {
            templates.getOrDefault(
                if (entity.isVisuallySwimming) AnimationType.SWIM
                else if (entity.animationSpeed > 1.5f) AnimationType.RUN
                else if (entity.isShiftKeyDown) AnimationType.WALK_SNEAKED
                else AnimationType.WALK, ""
            )
        } else {
            templates.getOrDefault(
                if (entity.isShiftKeyDown) AnimationType.IDLE_SNEAKED else AnimationType.IDLE,
                ""
            )
        }
    }

    override fun getModelLocation(): ResourceLocation {
        return "hc:models/entity/scene.gltf".rl
    }
}