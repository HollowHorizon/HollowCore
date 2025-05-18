package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexSorting
import de.fabmax.kool.input.PointerInput
import de.fabmax.kool.math.MutableVec2f
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.client.render.render
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun UiScope.Item(stack: ItemStack, scopeName: String? = null, block: ImageScope.() -> Unit) =
    GlCanvas(scopeName, { mouseX, mouseY, x, y, width, height ->
        stack.render(x, y, width, height)
    }) {
        modifier.size(16.dp, 16.dp)
            .border(RectBorder(Color.WHITE, 1.dp))
        block()
    }

inline fun UiScope.Entity(entity: LivingEntity, scopeName: String? = null, block: ImageScope.() -> Unit) =
    GlCanvas(scopeName, { mouseX, mouseY, x, y, width, height ->
        entity.render(x, y, width, height, 1f, mouseX.toFloat(), mouseY.toFloat(), 0f, 0f, true)
    }, block)

@OptIn(ExperimentalContracts::class)
inline fun UiScope.GlCanvas(
    scopeName: String? = null,
    crossinline glCanvas: ImageNode.(mouseX: Double, mouseY: Double, x: Float, y: Float, width: Float, height: Float) -> Unit,
    block: ImageScope.() -> Unit = {},
): ImageScope {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val image = uiNode.createChild(scopeName, ImageNode::class, ImageNode.factory)
    image.modifier.imageProvider(FlatImageProvider(WINDOW_BUFFER, true)).imageSize(ImageSize.Stretch)
    image.block()

    val x = image.leftPx
    val y = image.topPx
    val height = image.bottomPx - image.topPx
    val width = image.rightPx - image.leftPx

    surface.onEachFrame {

        drawGlCanvas(x, y, width, height, false, image.modifier.zLayer) { x, y, width, height ->
            GL33.glEnable(GL33.GL_DEPTH_TEST)
            GL33.glDepthFunc(GL33.GL_LEQUAL)
            val mouse = PointerInput.primaryPointer.pos
            image.glCanvas(mouse.x.toDouble(), mouse.y.toDouble(), x, y, width, height)
            GL33.glDisable(GL33.GL_DEPTH_TEST)
        }
    }

    val u0 = x / WINDOW_BUFFER.width
    val v0 = 1f - y / WINDOW_BUFFER.height

    val u1 = (x + width) / WINDOW_BUFFER.width
    val v1 = 1f - (y + height) / WINDOW_BUFFER.height

    image.modifier.imageProvider?.apply {
        (uvTopLeft as MutableVec2f).set(u0, v0)
        (uvTopRight as MutableVec2f).set(u1, v0)
        (uvBottomLeft as MutableVec2f).set(u0, v1)
        (uvBottomRight as MutableVec2f).set(u1, v1)
    }

    return image
}

fun drawGlCanvas(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    enableScissor: Boolean = true,
    zLayer: Int,
    renderable: (Float, Float, Float, Float) -> Unit,
) {
    val oldBuffer = GL33.glGetInteger(GL33.GL_FRAMEBUFFER_BINDING)
    val buffer = guiFramebuffer
    buffer.bindWrite(true)

    RenderSystem.backupProjectionMatrix()
    RenderSystem.setProjectionMatrix(
        Matrix4f().setOrtho(
            0.0F, buffer.width.toFloat(), buffer.height.toFloat(), 0.0F, 0.0F, 5000.0F
        ), VertexSorting.ORTHOGRAPHIC_Z
    )
    val matrix4fstack = RenderSystem.getModelViewStack()
    matrix4fstack.pushPose()
    matrix4fstack.setIdentity()
    matrix4fstack.translate(0.0f, 0.0f, -3000.0f + zLayer)
    RenderSystem.applyModelViewMatrix()
    if (enableScissor) RenderSystem.enableScissor(
        x.toInt(), (buffer.height - y - height).toInt(),
        width.toInt(), (height).toInt(),
    )

    RenderSystem.enableDepthTest()
    renderable(x, y, width, height)
    RenderSystem.disableDepthTest()

    if (enableScissor) RenderSystem.disableScissor()
    RenderSystem.restoreProjectionMatrix()

    matrix4fstack.popPose()

    RenderSystem.applyModelViewMatrix()
    RenderSystem.disableCull()

    buffer.unbindWrite()
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, oldBuffer)
}