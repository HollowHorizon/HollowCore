package ru.hollowhorizon.hc.client.models.internal

//? if <=1.19.2 {
import ru.hollowhorizon.hc.client.utils.toMc
//?}

import org.joml.Matrix4f
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.utils.use
import java.util.*

class Node(
    val index: Int,
    val children: List<Node>,
    val transform: Transformation,
    val mesh: Mesh? = null,
    val skin: Skin? = null,
    val name: String? = null,
) {
    val baseTransform = transform.copy()


    fun renderDecorations(context: RenderContext) {
        context.stack.use {
            //? if >=1.21 {
            /*mulPose(localMatrix)
            last().normal().mul(normalMatrix)
            *///?} elif >=1.20.1 {
            /*mulPoseMatrix(localMatrix)
            last().normal().mul(normalMatrix)
            *///?} else {
            mulPoseMatrix(localMatrix.toMc())
            last().normal().mul(normalMatrix.toMc())
            //?}

            context.entity?.let {
                context.nodeRenderer(it, this, this@Node, context.buffer, context.packedLight)
            }

            children.forEach { it.renderDecorations(context) }
        }
    }

    fun transformSkinning(stack: RenderCommands) {
        mesh?.transformSkinning(this@Node, stack)
        children.forEach { it.transformSkinning(stack) }
    }

    fun clearTransform() = transform.set(baseTransform)


    fun toLocal(transform: Transformation): Transformation {
        return baseTransform.copy().apply { sub(transform) }
    }

    fun fromLocal(transform: Transformation): Transformation {
        return baseTransform.copy().apply { add(transform) }
    }

    fun compile(context: RenderCommands) {
        mesh?.primitives?.forEach {
            it.compile(context, this)
        }
        children.forEach { it.compile(context) }
    }


    var parent: Node? = null
    val isHead: Boolean get() = name?.lowercase()?.contains("head") == true && parent?.isHead == false

    val globalMatrix: Matrix4f
        get() {
            return Matrix4f(NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.computeIfAbsent(this) {
                val matrix = Matrix4f(parent?.globalMatrix ?: return@computeIfAbsent localMatrix)
                return@computeIfAbsent matrix.mul(localMatrix)
            })
        }

    val globalRotation: Quaternionf
        get() {
            val rotation = parent?.globalRotation ?: return transform.rotation
            transform.apply {
                rotation.mulLeft(this.rotation)
            }
            return rotation
        }

    val localMatrix get() = transform.getMatrix()
    val normalMatrix get() = transform.getNormalMatrix()
}

val NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE = IdentityHashMap<Node, Matrix4f>()